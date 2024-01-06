package de.protubero.beanstore.builder;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.impl.BeanStoreImpl;
import de.protubero.beanstore.persistence.api.DeferredTransactionWriter;
import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.api.PersistentProperty;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;
import de.protubero.beanstore.tx.InstanceTransactionEvent;
import de.protubero.beanstore.tx.StoreWriter;
import de.protubero.beanstore.tx.Transaction;
import de.protubero.beanstore.tx.TransactionPhase;

public abstract class AbstractStoreBuilder {

	public static final Logger log = LoggerFactory.getLogger(AbstractStoreBuilder.class);
	
	
	protected TransactionPersistence persistence;

	protected boolean created;
	protected DeferredTransactionWriter deferredTransactionWriter;

	
	public AbstractStoreBuilder(TransactionPersistence persistence) {
		this.persistence = Objects.requireNonNull(persistence);
	}


	protected void throwExceptionIfAlreadyCreated() {
		// It can only be created once
		if (created) {
			throw new RuntimeException("bean store has already been created");
		}
	}

	
	protected void startBuildProcess() {
		throwExceptionIfAlreadyCreated();
		
		created = true;
		persistence.lockConfiguration();
		deferredTransactionWriter = new DeferredTransactionWriter(persistence.writer());
	}
	
	protected BeanStoreImpl endBuildProcess(ImmutableEntityStoreSet finalStoreSet) {
		if (deferredTransactionWriter != null) {
			// persist migration transactions
			// this is the first time that data gets written to the file
			deferredTransactionWriter.switchToNonDeferred();
		}	
		

		Runnable onCloseStoreAction = () -> {try {
			if (deferredTransactionWriter != null) {
				log.info("Closing transaction writer");
				deferredTransactionWriter.close();
			}	
		} catch (Exception e) {
			log.error("Error closing transaction writer", e);
		}};			
		
		BeanStoreImpl beanStoreImpl = new BeanStoreImpl(finalStoreSet, onCloseStoreAction, createStoreWriter());
		return beanStoreImpl;
	}

	
	private int lastReadTransactionId = -1; 
	@SuppressWarnings("unchecked")
	private MutableEntityStoreSet load(TransactionReader reader) {
		final MutableEntityStoreSet store = new MutableEntityStoreSet();
		store.setAcceptNonGeneratedIds(true);
		
		reader.load(pt -> {
			if (lastReadTransactionId == -1) {
				lastReadTransactionId = pt.getSeqNum();
				if (lastReadTransactionId != 1) {
					throw new RuntimeException("First transaction sequence num != 0");
				}
			} else {
				if (lastReadTransactionId  != (pt.getSeqNum() - 1)) {
					throw new RuntimeException("Transaction sequence broken " + lastReadTransactionId + " -> " + pt.getSeqNum());
				}
				lastReadTransactionId = pt.getSeqNum();
			}
			
			
			// log list of migration transactions
			if (pt.getTransactionType() == PersistentTransaction.TRANSACTION_TYPE_MIGRATION) {
				onReadMigrationTransaction(pt);
			}

			PersistentInstanceTransaction[] instanceTransactions = pt.getInstanceTransactions();
			if (instanceTransactions == null) {
				if (pt.getTransactionId() == null) {
					throw new RuntimeException("empty transaction");
				} else {
					instanceTransactions = new PersistentInstanceTransaction[0];
				}
			}

			for (PersistentInstanceTransaction pit : instanceTransactions) {
				@SuppressWarnings({ "rawtypes" })
				MutableEntityStore entityStore = (MutableEntityStore) store.storeOptional(pit.getAlias())
						.orElseGet(() -> {
							return (MutableEntityStore) store.register(new MapObjectCompanion(pit.getAlias()));
						});

				AbstractPersistentObject instance = null;
				switch (pit.getType()) {
				case PersistentInstanceTransaction.TYPE_CREATE:
					instance = entityStore.companion().createInstance();
					instance.id(pit.getId());
					instance.state(State.PREPARE);
					entityStore.put(instance);
					
					break;
				case PersistentInstanceTransaction.TYPE_DELETE:
					if (entityStore.remove(pit.getId()) == null) {
						throw new AssertionError();
					}
					break;
				case PersistentInstanceTransaction.TYPE_UPDATE:
					instance = entityStore.get(pit.getId());
					if (instance == null) {
						throw new AssertionError();
					}
					break;
				default:
					throw new AssertionError();
				}

				// update fields
				switch (pit.getType()) {
				case PersistentInstanceTransaction.TYPE_CREATE:
				case PersistentInstanceTransaction.TYPE_UPDATE:
					instance.version(pit.getVersion());
					if (pit.getPropertyUpdates() != null) {
						for (PersistentProperty propertyUpdate : pit.getPropertyUpdates()) {
							instance.put(propertyUpdate.getProperty(), propertyUpdate.getValue());
						}
					}
				}

			}
			
			onReadTransaction(pt);
		});
		
		// set state of all instances to 'Stored'
		store.forEach(es -> {
			es.objects().forEach(instance -> instance.state(State.STORED));
		});
		
		store.version(lastReadTransactionId);
		return store;
	}
	
	
	protected abstract void onReadTransaction(PersistentTransaction pt);

	protected abstract void onWriteTransaction(PersistentTransaction pt);

	protected abstract void onReadMigrationTransaction(PersistentTransaction pt);

	protected StoreWriter createStoreWriter() {
		StoreWriter storeWriter = new StoreWriter();

		storeWriter.registerSyncInternalTransactionListener(TransactionPhase.PERSIST, t -> {
			PersistentTransaction pTransaction = createTransaction(t);

			onWriteTransaction(pTransaction);
			
			if (deferredTransactionWriter != null) {
				deferredTransactionWriter.append(pTransaction);
			}
		});
		
		return storeWriter;
	}
	

	protected MutableEntityStoreSet loadMapStore() {

		boolean noStoredTransactions = persistence.isEmpty();
		if (noStoredTransactions) {
			return null;
		} else {
			return load(persistence.reader());
		}

	}
	
	private PersistentTransaction createTransaction(Transaction transaction) {
		PersistentTransaction pt = new PersistentTransaction(transaction.getTransactionType(), transaction.getTransactionId());		
		pt.setTimestamp(Objects.requireNonNull(transaction.getTimestamp()));
		if (transaction.getTargetStateVersion() == null) {
			throw new AssertionError();
		}
		pt.setSeqNum(transaction.getTargetStateVersion().intValue());
		
		PersistentInstanceTransaction[] eventArray = new PersistentInstanceTransaction[transaction.getInstanceEvents().size()];
		int idx = 0;
		for (InstanceTransactionEvent<?> event : transaction.getInstanceEvents()) {
			PersistentInstanceTransaction pit = new PersistentInstanceTransaction();
			eventArray[idx++] = pit;
			pit.setAlias(event.entity().alias());
			switch (event.type()) {
			case Delete:
				pit.setType(PersistentInstanceTransaction.TYPE_DELETE);
				pit.setId(event.replacedInstance().id());
				pit.setVersion(event.replacedInstance().version());
				break;
			case Update:
				pit.setType(PersistentInstanceTransaction.TYPE_UPDATE);
				pit.setId(event.newInstance().id());
				pit.setPropertyUpdates((PersistentProperty[]) event.values());
				pit.setVersion(event.newInstance().version());
				
				break;
			case Create:
				pit.setType(PersistentInstanceTransaction.TYPE_CREATE);
				pit.setId(event.newInstance().id());
				pit.setPropertyUpdates((PersistentProperty[]) event.values());
				pit.setVersion(event.newInstance().version());

				break;
			}
		}
		pt.setInstanceTransactions(eventArray);
		
		return pt;
	}
	
	
}
