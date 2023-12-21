package de.protubero.beanstore.writer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.Transition;
import de.protubero.beanstore.base.entity.BeanStoreException;
import de.protubero.beanstore.base.entity.Companion;
import de.protubero.beanstore.base.tx.InstanceTransactionEvent;
import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.base.tx.TransactionFailure;
import de.protubero.beanstore.base.tx.TransactionPhase;
import de.protubero.beanstore.persistence.base.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.base.PersistentProperty;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.store.CompanionShip;

public final class Transaction implements TransactionEvent {
	
	public static final Logger log = LoggerFactory.getLogger(Transaction.class);
	
	private CompanionShip companionSet;
	public PersistentTransaction persistentTransaction;

	private TransactionPhase transactionPhase = TransactionPhase.INITIAL;
	
	private List<StoreInstanceTransaction<?>> instanceTransactions;
	
	private boolean prepared;
	private TransactionFailure failure;
		
	private Transaction(CompanionShip companionSet, PersistentTransaction persistentTransaction) {
		this.companionSet = Objects.requireNonNull(companionSet);
		this.persistentTransaction = Objects.requireNonNull(persistentTransaction);
	}
	
	public static Transaction of(CompanionShip companionSet, 
			String transactionId, byte transactionType) {
		var pt = new PersistentTransaction(transactionType, transactionId);
		return new Transaction(companionSet, pt);
	}	

	public static Transaction of(CompanionShip companionSet, 
			String transactionId) {
		return of(companionSet, transactionId, PersistentTransaction.TRANSACTION_TYPE_DEFAULT);
	}	

	public static Transaction of(CompanionShip companionSet) {
		return of(companionSet, null, PersistentTransaction.TRANSACTION_TYPE_DEFAULT);
	}	
	
	public boolean isEmpty() {
		return persistentTransaction.getInstanceTransactions() == null 
				|| persistentTransaction.getInstanceTransactions().length == 0; 
	}
	
	public <T extends AbstractPersistentObject> T create(String alias) {
		Optional<Companion<T>> companion = companionSet.companionByAlias(alias);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid alias: " + alias);
		}
		T result = companion.get().createInstance();
		result.applyTransition(Transition.INSTANTIATED_TO_NEW);
		persistentTransaction.create(result.alias(), null).setRef(result);
		return result;
	}

		
	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> T create(T instance) {
		String alias = AbstractPersistentObject.aliasOf(instance);
		T result = create(alias);
		result.putAll(((Companion<T>) result.companion()).extractProperties(instance));
		return result;
	}
	
	
	public <T extends AbstractEntity> T create(Class<T> aClass) {
		Optional<Companion<T>> companion = companionSet.companionByClass(aClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class: " + aClass);
		}
		T result = companion.get().createInstance();
		result.applyTransition(Transition.INSTANTIATED_TO_NEW);
		persistentTransaction.create(result.alias(), null).setRef(result);
		return result;
	}
	
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
		Optional<Companion<AbstractPersistentObject>> companion = companionSet.companionByAlias(alias);
		
		if (companion.isEmpty()) {			
			throw new BeanStoreException("unknown alias: " + alias);
		}
		
		return alias;
	}

	public <T extends AbstractEntity> void delete(Class<T> aClass, long id) {
		deleteTx(companionSet.companionByClass(aClass).map(c -> c.alias()).orElseThrow(() -> {
			throw new RuntimeException("Invalid entity class: " + aClass);
		}), id);
	}
	
	public <T extends AbstractPersistentObject> void delete(T instance) {
		if (instance.state() != State.READY) {
			throw new RuntimeException("not a persistent instance");
		}
		deleteTx(instance.alias(), instance.id()).setRef(instance);
	}

	public <T extends AbstractPersistentObject> T update(T instance) {
		if (instance.state() != State.READY) {
			throw new BeanStoreException("not a persistent instance");
		}
		
		
		T detachedInstance = instance.detach();
		persistentTransaction.update(instance.alias(), instance.id()).setRef(detachedInstance);
		return detachedInstance;
	}
	
	private PersistentProperty[] asPropertyUpdates(Map<String, Object> changes) {
		if (changes == null || changes.size() == 0) {
			return null;
		}
		
		PersistentProperty[] propertyUpdates = new PersistentProperty[changes.size()];
		
		int idx = 0;
		for (Map.Entry<String, Object> entry : changes.entrySet()) {
			propertyUpdates[idx++] = PersistentProperty.of(entry.getKey(), entry.getValue());
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
		
		instanceTransactions = new ArrayList<>();
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


}	