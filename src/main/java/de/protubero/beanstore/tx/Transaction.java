package de.protubero.beanstore.tx;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreException;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.MapObject;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.store.CompanionSet;

public final class Transaction implements TransactionEvent {
	
	public static final Logger log = LoggerFactory.getLogger(Transaction.class);
	
	private CompanionSet companionSet;
	private String transactionId;
	private byte transactionType;
	private Instant timestamp;	
	private Integer targetStateVersion;
	private Integer sourceStateVersion;

	private TransactionPhase transactionPhase = TransactionPhase.INITIAL;
	
	private List<TransactionElement<?>> elements = new ArrayList<>();
	
	private TransactionFailure failure;
		
	private Transaction(CompanionSet companionSet, String transactionId, byte transactionType) {
		this.companionSet = Objects.requireNonNull(companionSet);
		this.transactionId = transactionId;
		this.transactionType = transactionType;
	}
	
	public static Transaction of(CompanionSet companionSet, 
			String transactionId, byte transactionType) {
		return new Transaction(companionSet, transactionId, transactionType);
	}	

	public static Transaction of(CompanionSet companionSet, 
			String transactionId) {
		return of(companionSet, transactionId, PersistentTransaction.TRANSACTION_TYPE_DEFAULT);
	}	

	public static Transaction of(CompanionSet companionSet) {
		return of(companionSet, null, PersistentTransaction.TRANSACTION_TYPE_DEFAULT);
	}	
	
	public boolean isEmpty() {
		return elements.isEmpty(); 
	}
	
	private <T extends AbstractPersistentObject> T create(Companion<T> companion) {
		T result = companion.createInstance();
		result.state(State.RECORD);
		
		TransactionElement<T> elt = new TransactionElement<>(
				this,
				InstanceEventType.Create,
				companion, 
				null, 
				result,
				null);
		elements.add(elt);
		
		return result;
	}
	
	public AbstractPersistentObject create(String alias) {
		Optional<Companion<? extends AbstractPersistentObject>> companion = companionSet.companionByAlias(alias);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid alias: " + alias);
		}
		return create(companion.get());
	}

	public <T extends AbstractEntity> T create(Class<T> aClass) {
		Optional<Companion<T>> companion = companionSet.companionByClass(aClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class: " + aClass);
		}
		return create(companion.get());
	}

	public <T extends AbstractPersistentObject> T create(T instance) {
		@SuppressWarnings("unchecked")
		Companion<T> companion = (Companion<T>) instance.companion();
		if (companion == null) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<Companion<T>> companionOpt = companionSet.companionByClass((Class) instance.getClass());
			if (companionOpt.isEmpty()) {
				throw new RuntimeException("Invalid entity class: " + instance.getClass());
			} else {
				companion = companionOpt.get();
			}
		}
		T result = create(companion);
		companion.transferProperties(instance, result);
		return result;
	}
	

	public void deleteOptLocked(String alias, long id, int version) {
		Optional<Companion<? extends AbstractPersistentObject>> companion = companionSet.companionByAlias(alias);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid alias: " + alias);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		TransactionElement<AbstractPersistentObject> elt = new TransactionElement<AbstractPersistentObject>(
				this,
				InstanceEventType.Delete,
				(Companion) companion.get(), 
				id, 
				null,
				null);
		elt.setVersion(version);
		elements.add(elt);
	}

	public void delete(String alias, long id) {
		Optional<Companion<? extends AbstractPersistentObject>> companion = companionSet.companionByAlias(alias);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid alias: " + alias);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		TransactionElement<AbstractPersistentObject> elt = new TransactionElement<AbstractPersistentObject>(
				this,
				InstanceEventType.Delete,
				(Companion) companion.get(), 
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
				this,
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
				this,
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
				this,
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
				this,
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
				this,
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
				this,
				InstanceEventType.Update,
				(Companion<T>) instance.companion(), 
				instance.id(), 
				recordInstance,
				instance);
		elements.add(elt);

		return recordInstance;
	}

	
	public <T extends AbstractEntity> T updateOptLocked(Class<T> aClass, long id, int version) {
		Optional<Companion<T>> companion = companionSet.companionByClass(aClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class: " + aClass);
		}
		
		T recordInstance = (T) companion.get().createInstance();
		recordInstance.state(State.RECORD);
		recordInstance.id(id);
		recordInstance.version(version);

		TransactionElement<T> elt = new TransactionElement<>(
				this,
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

	
	public MapObject updateMapObject(String alias, long id) {
		Optional<Companion<? extends AbstractPersistentObject>> companion = companionSet.companionByAlias(alias);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity alias: " + alias);
		}
		if (companion.get().isBean()) {
			throw new RuntimeException("Invalid entity alias (map alias expected): " + alias);
		}
		
		MapObject recordInstance = (MapObject) companion.get().createInstance();
		recordInstance.id(id);
		recordInstance.state(State.RECORD);

		TransactionElement<MapObject> elt = new TransactionElement<>(
				this,
				InstanceEventType.Update,
				(MapObjectCompanion) companion.get(), 
				id, 
				recordInstance,
				null);
		elements.add(elt);

		return recordInstance;
	}
	
	public <T extends AbstractEntity> T update(Class<T> aClass, long id) {
		Optional<Companion<T>> companion = companionSet.companionByClass(aClass);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid entity class: " + aClass);
		}
		
		T recordInstance = (T) companion.get().createInstance();
		recordInstance.id(id);
		recordInstance.state(State.RECORD);

		TransactionElement<T> elt = new TransactionElement<>(
				this,
				InstanceEventType.Update,
				(Companion<T>) companion.get(), 
				id, 
				recordInstance,
				null);
		elements.add(elt);

		return recordInstance;
	}
	
	
	@Override
	public TransactionPhase phase() {
		return transactionPhase;
	}

	public void setTransactionPhase(TransactionPhase transactionPhase) {
		this.transactionPhase = transactionPhase;
	}

	@Override
	public TransactionFailure failure() {
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

	@Override
	public Integer getTargetStateVersion() {
		return targetStateVersion;
	}

	void setTargetStateVersion(Integer targetStateVersion) {
		this.targetStateVersion = targetStateVersion;
	}

	@Override
	public Integer getSourceStateVersion() {
		return sourceStateVersion;
	}

	void setSourceStateVersion(Integer sourceStateVersion) {
		this.sourceStateVersion = sourceStateVersion;
	}



}	