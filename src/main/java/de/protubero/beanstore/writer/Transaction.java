package de.protubero.beanstore.writer;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.AbstractPersistentObject.State;
import de.protubero.beanstore.base.AbstractPersistentObject.Transition;
import de.protubero.beanstore.base.InstanceTransactionEvent;
import de.protubero.beanstore.persistence.base.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.base.PersistentPropertyUpdate;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.store.InstanceFactory;
import de.protubero.beanstore.store.BeanStoreReadAccess;
import de.protubero.beanstore.store.Store;

public class Transaction implements BeanStoreTransaction, TransactionEvent, MigrationTransaction {
	
	public static final Logger log = LoggerFactory.getLogger(Transaction.class);
	
	private BeanStoreReadAccess dataStore;
	private InstanceFactory context;
	public PersistentTransaction persistentTransaction;

	private TransactionPhase transactionPhase = TransactionPhase.INITIAL;
	
	private List<StoreInstanceTransaction<?>> instanceTransactions;
	
	private boolean prepared;
	private TransactionFailure failure;
		
	private Transaction(BeanStoreReadAccess dataStore, InstanceFactory context, PersistentTransaction persistentTransaction) {
		this.dataStore = dataStore;
		this.context = context;
		this.persistentTransaction = persistentTransaction;
	}
	
	public static Transaction of(BeanStoreReadAccess store, InstanceFactory iFactory, 
			String transactionId, int transactionType) {
		var pt = new PersistentTransaction(transactionType, transactionId);
		return new Transaction(store, iFactory, pt);
	}	
	
	public static Transaction of(Store store, 
			String transactionId, int transactionType) {
		return of(store, store, transactionId, transactionType);
	}	

	public static Transaction of(Store store) {
		return of(store, store, null, PersistentTransaction.TRANSACTION_TYPE_DEFAULT);
	}	
	
	public boolean isEmpty() {
		return persistentTransaction.getInstanceTransactions() == null 
				|| persistentTransaction.getInstanceTransactions().length == 0; 
	}
	
	@Override
	public <T extends AbstractPersistentObject> T create(String alias) {
		T result = context.newInstance(alias);
		result.applyTransition(Transition.INSTANTIATED_TO_NEW);
		persistentTransaction.create(result.alias(), result.id()).setRef(result);
		return result;
	}

	@Override
	public <T extends AbstractEntity> T create(Class<T> aClass) {
		T result = context.newInstance(aClass);
		result.applyTransition(Transition.INSTANTIATED_TO_NEW);
		persistentTransaction.create(result.alias(), result.id()).setRef(result);
		return result;
	}
	
	@Override
	public <T extends AbstractPersistentObject> void delete(String alias, long id) {
		// fail fast on invalid alias
		deleteTx(verifyAlias(alias), id);
	}
	
	private PersistentInstanceTransaction deleteTx(String alias, long id) {
		if (persistentTransaction.getInstanceTransactions() != null) {
			for (var tempTx : persistentTransaction.getInstanceTransactions()) {
				if (tempTx.getType() == PersistentInstanceTransaction.TYPE_DELETE
						&& tempTx.getAlias().equals(alias) 
						&& tempTx.getId().equals(id)) {
					throw new RuntimeException("duplicate deletion of " + alias + "/" + id);
				}
			}
		}
		
		return persistentTransaction.delete(alias, id);
	}

	private String verifyAlias(String alias) {
		if (!dataStore.exists(alias)) {			
			throw new RuntimeException("unknown alais: " + alias);
		}
		
		return alias;
	}
	

	@Override
	public <T extends AbstractEntity> void delete(Class<T> aClass, long id) {
		deleteTx(dataStore.entity(aClass).get().alias(), id);
	}
	
	@Override
	public <T extends AbstractPersistentObject> void delete(T instance) {
		if (instance.state() != State.READY) {
			throw new RuntimeException("not a persistent instance");
		}
		deleteTx(instance.alias(), instance.id()).setRef(instance);
	}

	@Override
	public <T extends AbstractPersistentObject> T update(T instance) {
		if (instance.state() != State.READY) {
			throw new RuntimeException("not a persistent instance");
		}
		T detachedInstance = instance.detach();
		persistentTransaction.update(instance.alias(), instance.id()).setRef(detachedInstance);
		return detachedInstance;
	}
	
	private PersistentPropertyUpdate[] asPropertyUpdates(Map<String, Object> changes) {
		if (changes == null || changes.size() == 0) {
			return null;
		}
		
		PersistentPropertyUpdate[] propertyUpdates = new PersistentPropertyUpdate[changes.size()];
		
		int idx = 0;
		for (Map.Entry<String, Object> entry : changes.entrySet()) {
			propertyUpdates[idx++] = PersistentPropertyUpdate.of(entry.getKey(), entry.getValue());
		}
		
		return propertyUpdates;
	}
	
	public void prepare() {
		if (prepared) {
			return;
		}
		prepared = true;
		
		persistentTransaction.setTimestamp(Instant.now());
		if (persistentTransaction.getInstanceTransactions() != null) {
			for (PersistentInstanceTransaction pit : persistentTransaction.getInstanceTransactions()) {
				AbstractPersistentObject apoRef = (AbstractPersistentObject) pit.getRef();
				if (pit.getType() == PersistentInstanceTransaction.TYPE_CREATE && pit.getRef() != null) {
					pit.setPropertyUpdates(asPropertyUpdates(apoRef.changes()));
				} else if (pit.getType() == PersistentInstanceTransaction.TYPE_UPDATE && pit.getRef() != null) {
					pit.setPropertyUpdates(asPropertyUpdates(apoRef.changes()));
					pit.setRef(apoRef.refInstance());
				}
			}
		}	
	}
	
	
	
	@Override
	public TransactionPhase phase() {
		return transactionPhase;
	}

	public void setTransactionPhase(TransactionPhase transactionPhase) {
		this.transactionPhase = transactionPhase;
	}

	public List<StoreInstanceTransaction<?>> getInstanceTransactions() {
		return instanceTransactions;
	}

	public void setInstanceTransactions(List<StoreInstanceTransaction<?>> instanceTransactions) {
		this.instanceTransactions = instanceTransactions;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<InstanceTransactionEvent<?>> getInstanceEvents() {
		return (List) instanceTransactions;
	}

	public boolean isPrepared() {
		return prepared;
	}

	@Override
	public boolean failed() {
		return failure != null;
	}

	@Override
	public TransactionFailure exception() {
		return failure;
	}

	public void setFailure(TransactionFailure failure) {
		this.failure = failure;
	}


	@Override
	public BeanStoreReadAccess read() {
		return dataStore;
	}


}	