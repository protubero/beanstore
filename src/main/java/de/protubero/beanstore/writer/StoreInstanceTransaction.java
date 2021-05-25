package de.protubero.beanstore.writer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.InstanceTransactionEvent;
import de.protubero.beanstore.base.InstancePropertyValue;
import de.protubero.beanstore.base.BeanStoreEntity;
import de.protubero.beanstore.persistence.base.PersistentInstanceTransaction;
import de.protubero.beanstore.store.EntityStore;

public final class StoreInstanceTransaction<T extends AbstractPersistentObject> implements InstanceTransactionEvent<T> {

	private PersistentInstanceTransaction persistentTransaction;
	
	@JsonIgnore
	private T replacedInstance;
	
	@JsonIgnore
	private EntityStore<T> entityStore;

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

	public EntityStore<T> getEntityStore() {
		return entityStore;
	}

	public void setEntityStore(EntityStore<T> entityStore) {
		this.entityStore = entityStore;
	}

	@Override
	public InstanceEventType type() {
		switch (getType()) {
		case PersistentInstanceTransaction.TYPE_CREATE:
			return InstanceTransactionEvent.InstanceEventType.Create;
		case PersistentInstanceTransaction.TYPE_UPDATE:
			return InstanceTransactionEvent.InstanceEventType.Update;
		case PersistentInstanceTransaction.TYPE_DELETE:
			return InstanceTransactionEvent.InstanceEventType.Delete;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public InstancePropertyValue[] values() {
		return persistentTransaction.getPropertyUpdates();
	}


	@Override
	public BeanStoreEntity<T> entity() {
		return entityStore.getCompagnon();
	}
	
}
