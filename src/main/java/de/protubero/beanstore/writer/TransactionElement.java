package de.protubero.beanstore.writer;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.BeanStoreEntity;
import de.protubero.beanstore.base.entity.Companion;
import de.protubero.beanstore.base.tx.InstanceEventType;
import de.protubero.beanstore.base.tx.InstanceTransactionEvent;
import de.protubero.beanstore.persistence.base.KeyValuePair;

public class TransactionElement<T extends AbstractPersistentObject> implements InstanceTransactionEvent<T> {

	private InstanceEventType type;
	private Companion<T> companion;
	private Long id;
	private Integer version;
	private T recordInstance;
	private T refInstance;
	private T newInstance;
	private T replacedInstance;
	private boolean optimisticLocking;
	
	public TransactionElement(
			InstanceEventType type, 
			Companion<T> companion, 
			Long id,
			T recordInstance,
			T refInstance) {
		this.type = type;
		this.companion = companion;
		this.id = id;
		this.recordInstance = recordInstance;
		this.refInstance = refInstance;
	}
	
	@Override
	public InstanceEventType type() {
		return type;
	}
	public String getAlias() {
		return companion.alias();
	}
	public AbstractPersistentObject getRecordInstance() {
		return recordInstance;
	}

	@Override
	public Long instanceId() {
		return id;
	}

	@Override
	public T replacedInstance() {
		return replacedInstance;
	}

	@Override
	public T newInstance() {
		return newInstance;
	}

	@Override
	public KeyValuePair[] values() {
		if (recordInstance == null) {
			return null;
		}
		return recordInstance.changes();
	}

	@Override
	public BeanStoreEntity<T> entity() {
		return companion;
	}

	void setNewInstance(T newInstance) {
		this.newInstance = newInstance;
	}

	void setReplacedInstance(T replacedInstance) {
		this.replacedInstance = replacedInstance;
	}

	public T getRefInstance() {
		return refInstance;
	}

	public Companion<T> getCompanion() {
		return companion;
	}

	public Long getId() {
		return id;
	}

	public boolean isOptimisticLocking() {
		return optimisticLocking;
	}

	void setOptimisticLocking(boolean optimisticLocking) {
		this.optimisticLocking = optimisticLocking;
	}

	public Integer getVersion() {
		return version;
	}

	void setVersion(Integer version) {
		this.version = version;
	}
	
	
}
