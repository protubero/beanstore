package de.protubero.beanstore.writer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.BeanStoreEntity;
import de.protubero.beanstore.base.tx.InstanceEventType;
import de.protubero.beanstore.base.tx.InstancePropertyValue;
import de.protubero.beanstore.base.tx.InstanceTransactionEvent;
import de.protubero.beanstore.persistence.base.PersistentInstanceTransaction;

public final class StoreInstanceTransaction<T extends AbstractPersistentObject> implements InstanceTransactionEvent<T> {

	private PersistentInstanceTransaction persistentTransaction;
	
	@JsonIgnore
	private T replacedInstance;
	

	@JsonIgnore
	private T newInstance;
	
	public int getType() {
		return persistentTransaction.getType();
	}
	
	@Override
	public Long instanceId() {
		return persistentTransaction.getId();
	}

	public PersistentInstanceTransaction getPersistentTransaction() {
		return persistentTransaction;
	}

	public void setPersistentTransaction(PersistentInstanceTransaction persistentTransaction) {
		this.persistentTransaction = persistentTransaction;
	}

	@Override
	public T replacedInstance() {
		return replacedInstance;
	}

	public void setReplacedInstance(T replacedInstance) {
		this.replacedInstance = replacedInstance;
	}

	@Override
	public T newInstance() {
		return newInstance;
	}

	public void setNewInstance(T newInstance) {
		this.newInstance = newInstance;
	}


	@Override
	public InstanceEventType type() {
		switch (getType()) {
		case PersistentInstanceTransaction.TYPE_CREATE:
			return InstanceEventType.Create;
		case PersistentInstanceTransaction.TYPE_UPDATE:
			return InstanceEventType.Update;
		case PersistentInstanceTransaction.TYPE_DELETE:
			return InstanceEventType.Delete;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public InstancePropertyValue[] values() {
		return persistentTransaction.getPropertyUpdates();
	}


	@SuppressWarnings("unchecked")
	@Override
	public BeanStoreEntity<T> entity() {
		return (BeanStoreEntity<T>) replacedInstance.companion();
	}
	
}
