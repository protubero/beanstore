package de.protubero.beanstore.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.Transition;
import de.protubero.beanstore.base.entity.Compagnon;
import de.protubero.beanstore.base.tx.InstanceTransactionEvent;
import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.base.tx.TransactionFailure;
import de.protubero.beanstore.base.tx.TransactionFailureType;
import de.protubero.beanstore.base.tx.TransactionPhase;
import de.protubero.beanstore.persistence.base.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.base.PersistentPropertyUpdate;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.store.EntityStore;
import de.protubero.beanstore.store.Store;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class StoreWriter  {

	public static final Logger log = LoggerFactory.getLogger(StoreWriter.class);
	
	// synchronous callbacks
	private List<Consumer<Transaction>> transactionListener = new ArrayList<>();
	private List<Consumer<StoreInstanceTransaction<?>>> instanceTransactionListener = new ArrayList<>();	

	// async callbacks
	private PublishSubject<Transaction> transactionSubject = PublishSubject.create(); 	
	private PublishSubject<StoreInstanceTransaction<?>> instanceTransactionSubject = PublishSubject.create(); 	
	
		
	protected Store store;

	public StoreWriter(Store store) {
		this.store = Objects.requireNonNull(store);
		
		transactionSubject.
			subscribe(tx -> {
			List<StoreInstanceTransaction<?>> instanceTransactions = tx.getInstanceTransactions();
			if (instanceTransactions != null) {
				instanceTransactions.forEach(itx -> instanceTransactionSubject.onNext(itx));
			}
		});		
	}
	
	public void verify(Consumer<TransactionEvent> consumer) {
		registerSyncTransactionListener(TransactionPhase.VERIFICATION, consumer);
	}

	public void onChange(Consumer<TransactionEvent> consumer) {
		registerSyncTransactionListener(TransactionPhase.COMMITTED_SYNC, consumer);
	}

	public void onChangeAsync(Consumer<TransactionEvent> consumer) {
		transactionSubject
			.subscribeOn(Schedulers.single())				
			.subscribe(tx -> {
				System.out.println("accept");
				consumer.accept(tx);
			});
	}

	public void verifyInstance(Consumer<InstanceTransactionEvent<?>> consumer) {
		registerSyncInstanceTransactionListener(TransactionPhase.VERIFICATION, consumer);
	}

	public void onChangeInstance(Consumer<InstanceTransactionEvent<?>> consumer) {
		registerSyncInstanceTransactionListener(TransactionPhase.COMMITTED_SYNC, consumer);
	}

	public void onChangeInstanceAsync(Consumer<InstanceTransactionEvent<?>> consumer) {
		instanceTransactionSubject
			.subscribeOn(Schedulers.single())		
			.subscribe(itx -> {
				consumer.accept(itx);
			});
	}
	
	
	public void registerSyncTransactionListener(TransactionPhase phase, final Consumer<TransactionEvent> listener) {
		transactionListener.add((transaction) -> {
			if (transaction.phase() == phase) {
				listener.accept(transaction);
			}
		});
	}

	public void registerSyncInternalTransactionListener(TransactionPhase phase, final Consumer<Transaction> listener) {
		transactionListener.add((transaction) -> {
			if (transaction.phase() == phase) {
				listener.accept(transaction);
			}
		});
	}
	
	public void registerSyncInstanceTransactionListener(TransactionPhase phase, final Consumer<InstanceTransactionEvent<?>> listener) {
		transactionListener.add((transaction) -> {
			if (transaction.phase() == phase) {
				transaction.getInstanceTransactions().forEach(listener);
			}
		});
	}
	
	public void notifyTransactionListener(Transaction transaction, Consumer<Exception> exceptionHandler) {
		for (Consumer<Transaction> aTransactionListener : this.transactionListener) {
			try {
				aTransactionListener.accept(transaction);
			} catch (Exception e) {
				exceptionHandler.accept(e);
			}
		}
		for (Consumer<StoreInstanceTransaction<?>> aTransactionListener : this.instanceTransactionListener) {
			for (var sit : transaction.getInstanceTransactions()) {
				try {
					aTransactionListener.accept(sit);
				} catch (Exception e) {
					exceptionHandler.accept(e);
				}
			}
		}
	}
	
	public synchronized Store snapshot() {
		return store.snapshot();
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void execute(Transaction aTransaction) throws TransactionFailure {
		aTransaction.prepare();		
		aTransaction.setInstanceTransactions(new ArrayList<>());

		if (!aTransaction.isEmpty()) {		
					
			// 1. Create clones and check optimistic locking (Wrap with StoreInstanceTransaction)
			for (PersistentInstanceTransaction pit : aTransaction.persistentTransaction.getInstanceTransactions()) {
				EntityStore<?> entityStore = store.store(pit.getAlias());
				Compagnon compagnon = ((Compagnon) entityStore.getCompagnon());
				AbstractPersistentObject newInstance = null;
				AbstractPersistentObject origInstance = null;
				if (pit.getType() == PersistentInstanceTransaction.TYPE_DELETE ||
						pit.getType() == PersistentInstanceTransaction.TYPE_UPDATE) {
					
					origInstance = entityStore.get(pit.getId());
					// fail if referenced instance doesn't exist or has already been deleted
					if (origInstance == null) {
						throw new TransactionFailure(TransactionFailureType.INSTANCE_NOT_FOUND, pit.getAlias(), pit.getId());
					} 
					
					// check optimistic locking
					if (pit.getRef() != null && pit.getRef() != origInstance) {
						throw new TransactionFailure(TransactionFailureType.OPTIMISTIC_LOCKING_FAILED, pit.getAlias(),  pit.getId());
					}
					
					newInstance = compagnon.cloneInstance(origInstance);
					
					if (pit.getType() == PersistentInstanceTransaction.TYPE_UPDATE && pit.getPropertyUpdates() != null) {
						// set properties
						for (PersistentPropertyUpdate ppu : pit.getPropertyUpdates()) {
							newInstance.put(ppu.getProperty(), ppu.getValue());						
						}
					}
					newInstance.applyTransition(Transition.INSTANTIATED_TO_READY);
	
				} else {
					long newInstanceId = entityStore.getNextInstanceId();	
					
					if (pit.getRef() == null) {
						newInstance = compagnon.createInstance(newInstanceId);
						// set properties
						for (PersistentPropertyUpdate ppu : pit.getPropertyUpdates()) {
							newInstance.put(ppu.getProperty(), ppu.getValue());						
						}
						pit.setRef(newInstance);
					} else {
						newInstance = (AbstractPersistentObject) pit.getRef(); 
						newInstance.id(newInstanceId);
					}
					pit.setId(newInstanceId);
					newInstance.applyTransition(Transition.NEW_TO_READY);
				}
				
				StoreInstanceTransaction sit = new StoreInstanceTransaction();
				sit.setPersistentTransaction(pit);
				sit.setNewInstance(newInstance);
				sit.setReplacedInstance(origInstance);
				sit.setEntityStore(entityStore);
				aTransaction.getInstanceTransactions().add(sit);
			}
			
			// 2. Verify Transaction / check invariants
			aTransaction.setTransactionPhase(TransactionPhase.VERIFICATION);
			notifyTransactionListener(aTransaction, (e) -> {throw new TransactionFailure(TransactionFailureType.VERIFICATION_FAILED, e);});
		}	
		
		if (!aTransaction.isEmpty() || (aTransaction.persistentTransaction.getTransactionType() == PersistentTransaction.TRANSACTION_TYPE_MIGRATION)) {		
			// 3. persist
			aTransaction.setTransactionPhase(TransactionPhase.PERSIST);
			notifyTransactionListener(aTransaction, (e) -> {throw new TransactionFailure(TransactionFailureType.PERSISTENCE_FAILED, e);});
		}	
		
		if (!aTransaction.isEmpty()) {		
			aTransaction.setTransactionPhase(TransactionPhase.EXECUTE);
			// 4. apply changes
			for (StoreInstanceTransaction<?> sit : aTransaction.getInstanceTransactions()) {
				EntityStore entityStore = ((EntityStore) sit.getEntityStore()); 
				if (sit.getType() == PersistentInstanceTransaction.TYPE_DELETE) {
					AbstractPersistentObject removedInstance = entityStore.remove(sit.instanceId());
					if (removedInstance == null) {
						throw new AssertionError();
					}
					
					removedInstance.applyTransition(Transition.READY_TO_OUTDATED);
				} else {
					AbstractPersistentObject origInstance = entityStore.put((AbstractPersistentObject) sit.newInstance());
					if (origInstance != null) {
						origInstance.applyTransition(Transition.READY_TO_OUTDATED);
					}	
				}	
			}
			
			// 5. inform read models sync (for models which need to be in sync)
			aTransaction.setTransactionPhase(TransactionPhase.COMMITTED_SYNC);
			notifyTransactionListener(aTransaction, (e) -> {log.error("exection in COMMITTED_SYNC listener", e);});
			
			// 6. inform read models async 
			aTransaction.setTransactionPhase(TransactionPhase.COMMITTED_ASYNC);
			transactionSubject.onNext(aTransaction);
		}	
		
	}	

	public Store dataStore() {
		return store;
	}
	
	
}
