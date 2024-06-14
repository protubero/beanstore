package de.protubero.beanstore.tx;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.entity.PersistentObjectVersionKey;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.store.CompanionSet;

public final class Transaction implements TransactionEvent {
	
	public static final Logger log = LoggerFactory.getLogger(Transaction.class);

	private static AtomicLong newObjectId = new AtomicLong(-1);
	
	private String description;	
	private CompanionSet companionSet;
	private String migrationId;
	private byte transactionType;
	private Instant timestamp;	
	private Integer targetStateVersion;
	private Integer sourceStateVersion;

	private TransactionPhase transactionPhase = TransactionPhase.INITIAL;
	
	private List<TransactionElement<?>> elements = new ArrayList<>();
	
	// required to check duplicate update elements
	private Set<TransactionElement<?>> updElementSet = new HashSet<>();
	
	private TransactionFailure failure;
		
	private Transaction(CompanionSet companionSet, String migrationId, byte transactionType) {
		this.companionSet = Objects.requireNonNull(companionSet);
		this.migrationId = migrationId;
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
		result.id(newObjectId.addAndGet(-1));
		
		TransactionElement<T> elt = new TransactionElement<>(
				this,
				InstanceEventType.Create,
				companion, 
				result.id(), 
				result);
		addElement(elt);
		
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

	public <T extends AbstractPersistentObject> T create(T templateInstance) {
		@SuppressWarnings("unchecked")
		Companion<T> companion = (Companion<T>) templateInstance.companion();
		if (companion == null) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<Companion<T>> companionOpt = companionSet.companionByClass((Class) templateInstance.getClass());
			if (companionOpt.isEmpty()) {
				throw new RuntimeException("Invalid entity class: " + templateInstance.getClass());
			} else {
				companion = companionOpt.get();
			}
		}
		T result = create(companion);
		companion.transferProperties(templateInstance, result);
		return result;
	}

	
	public void delete(PersistentObjectKey<?> key) {
		delete(key, false);
	}
	
	public void delete(PersistentObjectKey<?> key, boolean ignoreNonExistence) {
		Objects.requireNonNull(key);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<Companion<?>> companion = (Optional) companionSet.companionByKey(key);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid key entity: " + key);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		TransactionElement<AbstractPersistentObject> elt = new TransactionElement<AbstractPersistentObject>(
				this,
				InstanceEventType.Delete,
				(Companion) companion.get(), 
				key.id(), 
				null);
		elt.setIgnoreNonExistence(ignoreNonExistence);
		
		addElement(elt);
	}
	

	
	/**
	 * Deletion of a specific version implies ignoreNonExistence == false
	 * 
	 * @param key
	 */
	public void delete(PersistentObjectVersionKey<?> key) {
		Objects.requireNonNull(key);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<Companion<?>> companion = (Optional) companionSet.companionByKey(key);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid key entity: " + key);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		TransactionElement<AbstractPersistentObject> elt = new TransactionElement<AbstractPersistentObject>(
				this,
				InstanceEventType.Delete,
				(Companion) companion.get(), 
				key.id(), 
				null);
		elt.setVersion(key.version());
		addElement(elt);
	}

	public <T extends AbstractPersistentObject> T update(PersistentObjectKey<T> key) {
		Optional<Companion<T>> companion = companionSet.companionByKey(key);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid key entity: " + key);
		}
		
		T recordInstance = (T) companion.get().createInstance();
		recordInstance.id(key.id());
		recordInstance.state(State.RECORD);

		TransactionElement<T> elt = new TransactionElement<>(
				this,
				InstanceEventType.Update,
				companion.get(), 
				key.id(), 
				recordInstance);
		addElement(elt);

		return recordInstance;
	}
	
	public <T extends AbstractPersistentObject> T update(PersistentObjectVersionKey<T> key) {
		Optional<Companion<T>> companion = companionSet.companionByKey(key);
		if (companion.isEmpty()) {
			throw new RuntimeException("Invalid key entity: " + key);
		}
		
		T recordInstance = (T) companion.get().createInstance();
		recordInstance.state(State.RECORD);
		recordInstance.id(key.id());
		recordInstance.version(key.version());

		TransactionElement<T> elt = new TransactionElement<>(
				this,
				InstanceEventType.Update,
				companion.get(), 
				key.id(), 
				recordInstance);
		elt.setOptimisticLocking(true);
		elt.setVersion(key.version());
		addElement(elt);

		return recordInstance;
	}
	
	private <T extends AbstractPersistentObject> void addElement(TransactionElement<T> elt) {
		if (elt.type() == InstanceEventType.Update) {
			if (updElementSet.contains(elt)) {
				throw new RuntimeException("Duplicate update transaction element " + elt);
			}
			updElementSet.add(elt);
		}
		elements.add(elt);
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

	public String getMigrationId() {
		return migrationId;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean containsDeletionOf(PersistentObjectKey<?> key) {
		return elements.stream().filter(elt -> elt.type() == InstanceEventType.Delete && 
				elt.getAlias().equals(key.alias()) && 
				elt.getId().longValue() == key.id()).findFirst().isPresent();
	}




}	