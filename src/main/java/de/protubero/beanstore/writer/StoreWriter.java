package de.protubero.beanstore.writer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.base.entity.Companion;
import de.protubero.beanstore.base.tx.InstanceTransactionEvent;
import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.base.tx.TransactionFailure;
import de.protubero.beanstore.base.tx.TransactionFailureType;
import de.protubero.beanstore.base.tx.TransactionPhase;
import de.protubero.beanstore.persistence.base.KeyValuePair;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.store.EntityStore;
import de.protubero.beanstore.store.EntityStoreSet;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class StoreWriter  {

	public static final Logger log = LoggerFactory.getLogger(StoreWriter.class);
	
	// synchronous callbacks
	private List<Consumer<Transaction>> transactionListener = new ArrayList<>();
	private List<Consumer<InstanceTransactionEvent<?>>> instanceTransactionListener = new ArrayList<>();	

	// async callbacks
	private PublishSubject<Transaction> transactionSubject = PublishSubject.create(); 	
	private PublishSubject<InstanceTransactionEvent<?>> instanceTransactionSubject = PublishSubject.create(); 	
	
		

	public StoreWriter() {
		transactionSubject.
			subscribe(tx -> {
			List<? extends InstanceTransactionEvent<?>> instanceTransactions = tx.getInstanceEvents();
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
				transaction.getInstanceEvents().forEach(listener);
			}
		});
	}
	
	public void notifyTransactionListener(Transaction transaction, Consumer<Exception> exceptionHandler) {
		for (Consumer<Transaction> aTransactionListener : this.transactionListener) {
			try {
				aTransactionListener.accept(transaction);
			} catch (Exception e) {
				log.error("Error in transaction listener", e);
				exceptionHandler.accept(e);
			}
		}
		for (Consumer<InstanceTransactionEvent<?>> aTransactionListener : this.instanceTransactionListener) {
			for (var sit : transaction.getInstanceEvents()) {
				try {
					aTransactionListener.accept(sit);
				} catch (Exception e) {
					log.error("Error in instance transaction listener", e);
					exceptionHandler.accept(e);
				}
			}
		}
	}
		
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized <E extends EntityStore<?>, S extends EntityStoreSet<E>> S execute(Transaction aTransaction, S aStoreSet) throws TransactionFailure {
		if (aTransaction.getTimestamp() != null) {
			throw new RuntimeException("Re-Execution of Transaction");
		}
		aTransaction.setTimestamp(Instant.now());
		
		// Clone Store Set
		S workStoreSet = (S) Objects.requireNonNull(aStoreSet).internalCloneStoreSet();
		S result = aStoreSet;
		
		
		if (!aTransaction.isEmpty()) {		
					
			// 1. Create instance clones and check optimistic locking (Wrap with StoreInstanceTransaction)
			for (TransactionElement<?> elt : aTransaction.elements()) {
				EntityStore<?> entityStore = workStoreSet.store(elt.getAlias());
				if (entityStore == null) {
					throw new AssertionError();
				}
				
				Companion companion = ((Companion) entityStore.companion());
				if (elt.getCompanion() != null && elt.getCompanion() != companion) {
					throw new AssertionError();
				}
				
				AbstractPersistentObject newInstance = null;
				AbstractPersistentObject origInstance = null;
				switch (elt.type()) {
				case Delete:
					if (elt.getId() == null) {
						throw new AssertionError();
					}
					
					origInstance = entityStore.get(elt.getId());
					
					// fail if referenced instance doesn't exist or has already been deleted
					if (origInstance == null) {
						throw new TransactionFailure(TransactionFailureType.INSTANCE_NOT_FOUND, elt);
					} 
					
					// check optimistic locking
					if (elt.isOptimisticLocking() && elt.getVersion().intValue() != origInstance.version()) {
						throw new TransactionFailure(TransactionFailureType.OPTIMISTIC_LOCKING_FAILED, elt);
					}
					
					((TransactionElement) elt).setReplacedInstance(origInstance);
					
					break;
				case Update:
					if (elt.getId() == null) {
						throw new AssertionError();
					}
					
					origInstance = entityStore.get(elt.getId());
					
					// fail if referenced instance doesn't exist or has already been deleted
					if (origInstance == null) {
						throw new TransactionFailure(TransactionFailureType.INSTANCE_NOT_FOUND, elt);
					} 
					
					// check optimistic locking
					if (elt.isOptimisticLocking() && elt.getVersion().intValue() != origInstance.version()) {
						throw new TransactionFailure(TransactionFailureType.OPTIMISTIC_LOCKING_FAILED, elt);
					}
					
					newInstance = companion.createInstance();
					newInstance.state(State.PREPARE);
					newInstance.id(origInstance.id());
					newInstance.version(origInstance.version() + 1);

					// transfer all properties from original instance
					companion.transferProperties(origInstance, newInstance);
					
					// overwrite updated properties
					elt.getRecordInstance().state(State.RECORDED);
					for (KeyValuePair kvp : elt.getRecordInstance().changes()) {
						newInstance.put(kvp.getProperty(), kvp.getValue());						
					}
					
					((TransactionElement) elt).setReplacedInstance(origInstance);
					((TransactionElement) elt).setNewInstance(newInstance);

					elt.getRecordInstance().version(newInstance.version());
					
					break;
				case Create:
					if (elt.getId() != null || elt.getVersion() != null || elt.getRefInstance() != null) {
						throw new AssertionError();
					}
					long newInstanceId =  entityStore.getAndIncreaseInstanceId();	
					
					newInstance = companion.createInstance(newInstanceId);
					// set properties
					newInstance.state(State.PREPARE);
					
					elt.getRecordInstance().state(State.RECORDED);					
					for (KeyValuePair kvp : elt.getRecordInstance().changes()) {
						newInstance.put(kvp.getProperty(), kvp.getValue());						
					}

					((TransactionElement) elt).setNewInstance(newInstance);
					elt.getRecordInstance().id(newInstanceId);
					elt.getRecordInstance().version(newInstance.version());
					break;
				}
				
			}
			
			// 2. Verify Transaction / check invariants
			aTransaction.setTransactionPhase(TransactionPhase.VERIFICATION);
			notifyTransactionListener(aTransaction, (e) -> {throw new TransactionFailure(TransactionFailureType.VERIFICATION_FAILED, e);});
		}	
		
		if (!aTransaction.isEmpty() || (aTransaction.getTransactionType() == PersistentTransaction.TRANSACTION_TYPE_MIGRATION)) {		
			// 3. persist
			aTransaction.setTransactionPhase(TransactionPhase.PERSIST);
			notifyTransactionListener(aTransaction, (e) -> {throw new TransactionFailure(TransactionFailureType.PERSISTENCE_FAILED, e);});
		}	
		
		if (!aTransaction.isEmpty()) {		
			aTransaction.setTransactionPhase(TransactionPhase.EXECUTE);
			
			// only return clones StoreSet if there are any changes
			result = workStoreSet;
			
			// 4. apply changes
			for (TransactionElement<?> elt : aTransaction.elements()) {
				EntityStore<?> entityStore = workStoreSet.store(elt.getAlias());
				switch (elt.type()) {
				case Delete:
					AbstractPersistentObject removedInstance = entityStore.internalRemoveInplace(elt.replacedInstance().id());
					if (removedInstance == null) {
						// this should have lead to an Transaction Failure already
						throw new AssertionError();
					}
					removedInstance.state(State.OUTDATED);
					break;
				case Update:
					elt.newInstance().state(State.STORED);
					AbstractPersistentObject origInstance = entityStore.internalUpdateInplace((AbstractPersistentObject) elt.newInstance());
					if (origInstance == null)  {
						// this should have lead to an TransactionFeature already
						throw new AssertionError();
					}
					origInstance.state(State.OUTDATED);
					break;
				case Create:
					elt.newInstance().state(State.STORED);
					AbstractPersistentObject existingInstance = entityStore.internalCreateInplace((AbstractPersistentObject) elt.newInstance());
					if (existingInstance != null) {
						// instance id management went wrong
						throw new AssertionError();
					}
					break;
				}
			}
			
			// 5. inform read models sync (for models which need to be in sync)
			aTransaction.setTransactionPhase(TransactionPhase.COMMITTED_SYNC);
			notifyTransactionListener(aTransaction, (e) -> {log.error("exection in COMMITTED_SYNC listener", e);});
			
			// 6. inform read models async 
			aTransaction.setTransactionPhase(TransactionPhase.COMMITTED_ASYNC);
			transactionSubject.onNext(aTransaction);
		}	
		
		return result;
	}	

	
	
}
