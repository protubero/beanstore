package de.protubero.beanstore.writer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.base.entity.BeanStoreException;
import de.protubero.beanstore.base.entity.Companion;
import de.protubero.beanstore.base.tx.InstanceEventType;
import de.protubero.beanstore.base.tx.InstanceTransactionEvent;
import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.base.tx.TransactionFailure;
import de.protubero.beanstore.base.tx.TransactionPhase;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.store.CompanionShip;

public final class Transaction implements TransactionEvent {
	
	public static final Logger log = LoggerFactory.getLogger(Transaction.class);
	
	private CompanionShip companionSet;
	private String transactionId;
	private byte transactionType;
	private Instant timestamp;	

	private TransactionPhase transactionPhase = TransactionPhase.INITIAL;
	
	private List<TransactionElement<?>> elements = new ArrayList<>();
	
	private TransactionFailure failure;
		
	private Transaction(CompanionShip companionSet, String transactionId, byte transactionType) {
		this.companionSet = Objects.requireNonNull(companionSet);
		this.transactionId = transactionId;
		this.transactionType = transactionType;
	}
	
	public static Transaction of(CompanionShip companionSet, 
			String transactionId, byte transactionType) {
		return new Transaction(companionSet, transactionId, transactionType);
	}	

	public static Transaction of(CompanionShip companionSet, 
			String transactionId) {
		return of(companionSet, transactionId, PersistentTransaction.TRANSACTION_TYPE_DEFAULT);
	}	

	public static Transaction of(CompanionShip companionSet) {
		return of(companionSet, null, PersistentTransaction.TRANSACTION_TYPE_DEFAULT);
	}	
	
	public boolean isEmpty() {
		return elements.isEmpty(); 
	}
	
	private <T extends AbstractPersistentObject> T create(Companion<T> companion) {
		T result = companion.createInstance();
		result.state(State.RECORD);
		
		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Create,
				companion, 
				null, 
				result,
				null);
		elements.add(elt);
		
		return result;
	}
	
	public <T extends AbstractPersistentObject> T create(String alias) {
		Optional<Companion<T>> companion = companionSet.companionByAlias(alias);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid alias: " + alias);
		}
		return create(companion.get());
	}

	public <T extends AbstractPersistentObject> T create(Class<T> aClass) {
		Optional<Companion<T>> companion = companionSet.companionByClass(aClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class: " + aClass);
		}
		return create(companion.get());
	}

	

	public <T extends AbstractPersistentObject> void deleteOptLocked(String alias, long id, int version) {
		Optional<Companion<T>> companion = companionSet.companionByAlias(alias);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid alias: " + alias);
		}

		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Delete,
				companion.get(), 
				id, 
				null,
				null);
		elt.setVersion(version);
		elements.add(elt);
	}

	public <T extends AbstractPersistentObject> void delete(String alias, long id) {
		Optional<Companion<T>> companion = companionSet.companionByAlias(alias);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid alias: " + alias);
		}

		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Delete,
				companion.get(), 
				id, 
				null,
				null);
		elements.add(elt);
	}
	

	public <T extends AbstractEntity> void deleteOptLocked(Class<T> aClass, long id, int version) {
		Optional<Companion<T>> companion = companionSet.companionByClass(aClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class: " + aClass);
		}

		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Delete,
				companion.get(), 
				id, 
				null,
				null);
		elt.setVersion(version);
		elements.add(elt);
		
	}
	
	public <T extends AbstractEntity> void delete(Class<T> aClass, long id) {
		Optional<Companion<T>> companion = companionSet.companionByClass(aClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class: " + aClass);
		}

		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Delete,
				companion.get(), 
				id, 
				null,
				null);
		elements.add(elt);
		
	}

	public <T extends AbstractPersistentObject> void deleteOptLocked(T instance) {
		if ((instance.state() != State.STORED) &&  (instance.state() != State.OUTDATED)) {
			throw new RuntimeException("not a persistent instance");
		}

		@SuppressWarnings("unchecked")
		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Delete,
				(Companion<T>) instance.companion(), 
				instance.id(),
				null,
				instance);
		elt.setOptimisticLocking(true);
		elt.setVersion(instance.version());
		elements.add(elt);
	}
	
	
	public <T extends AbstractPersistentObject> void delete(T instance) {
		if ((instance.state() != State.STORED) &&  (instance.state() != State.OUTDATED)) {
			throw new RuntimeException("not a persistent instance");
		}

		@SuppressWarnings("unchecked")
		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Delete,
				(Companion<T>) instance.companion(), 
				instance.id(),
				null,
				instance);
		elements.add(elt);
	}

	public <T extends AbstractPersistentObject> T updateOptLocked(T instance) {
		if ((instance.state() != State.STORED) &&  (instance.state() != State.OUTDATED)) {
			throw new BeanStoreException("not a persistent instance");
		}
		
		@SuppressWarnings("unchecked")
		T recordInstance = (T) instance.companion().createInstance();
		recordInstance.state(State.RECORD);
		recordInstance.id(instance.id());
		recordInstance.version(instance.version());

		@SuppressWarnings("unchecked")
		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Update,
				(Companion<T>) instance.companion(), 
				instance.id(), 
				recordInstance,
				instance);
		elt.setOptimisticLocking(true);
		elt.setVersion(instance.version());
		elements.add(elt);

		return recordInstance;
	}
	
	public <T extends AbstractPersistentObject> T update(T instance) {
		if ((instance.state() != State.STORED) &&  (instance.state() != State.OUTDATED)) {
			throw new BeanStoreException("not a persistent instance");
		}
		
		@SuppressWarnings("unchecked")
		T recordInstance = (T) instance.companion().createInstance();
		recordInstance.state(State.RECORD);
		recordInstance.id(instance.id());
		recordInstance.version(instance.version());

		@SuppressWarnings("unchecked")
		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Update,
				(Companion<T>) instance.companion(), 
				instance.id(), 
				recordInstance,
				instance);
		elements.add(elt);

		return recordInstance;
	}

	
	public <T extends AbstractPersistentObject> T updateOptLocked(Class<T> aClass, long id, int version) {
		Optional<Companion<T>> companion = companionSet.companionByClass(aClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class: " + aClass);
		}
		
		T recordInstance = (T) companion.get().createInstance();
		recordInstance.state(State.RECORD);

		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Update,
				(Companion<T>) companion.get(), 
				id, 
				recordInstance,
				null);
		elt.setOptimisticLocking(true);
		elt.setVersion(version);
		elements.add(elt);

		return recordInstance;
		
	}
	
	public <T extends AbstractPersistentObject> T update(Class<T> aClass, long id) {
		Optional<Companion<T>> companion = companionSet.companionByClass(aClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class: " + aClass);
		}
		
		T recordInstance = (T) companion.get().createInstance();
		recordInstance.state(State.RECORD);

		TransactionElement<T> elt = new TransactionElement<>(
				InstanceEventType.Update,
				(Companion<T>) companion.get(), 
				id, 
				recordInstance,
				null);
		elements.add(elt);

		return recordInstance;
	}
	
	
	/*
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
	}*/
	
	@Override
	public TransactionPhase phase() {
		return transactionPhase;
	}

	public void setTransactionPhase(TransactionPhase transactionPhase) {
		this.transactionPhase = transactionPhase;
	}

	/*
	public List<StoreInstanceTransaction<?>> getInstanceTransactions() {
		return instanceTransactions;
	}*/

	/*
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<InstanceTransactionEvent<?>> getInstanceEvents() {
		return (List) instanceTransactions;
	}
	*/

	/*
	public boolean isPrepared() {
		return prepared;
	}
	*/

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

	public String getTransactionId() {
		return transactionId;
	}

	public byte getTransactionType() {
		return transactionType;
	}

	List<TransactionElement<?>> elements() {
		return Collections.unmodifiableList(elements);
	}

	@Override
	public List<? extends InstanceTransactionEvent<?>> getInstanceEvents() {
		return elements;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}


}	