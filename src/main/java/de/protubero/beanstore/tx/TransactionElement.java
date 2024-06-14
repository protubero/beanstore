package de.protubero.beanstore.tx;

import java.util.Objects;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.persistence.api.KeyValuePair;

public class TransactionElement<T extends AbstractPersistentObject> implements InstanceTransactionEvent<T> {

	private InstanceEventType type;
	private Companion<T> companion;
	private Long id;
	private Integer version;
	private T recordInstance;
	private T newInstance;
	private T replacedInstance;
	private boolean optimisticLocking;
	private Transaction transaction;
	private boolean ignoreNonExistence;
	
	public TransactionElement(
			Transaction aTransaction,
			InstanceEventType type, 
			Companion<T> companion, 
			Long id,
			T recordInstance) {
		this.transaction = Objects.requireNonNull(aTransaction);
		this.type = type;
		this.companion = companion;
		this.id = id;
		this.recordInstance = recordInstance;
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

	
	@Override
	public TransactionEvent transactionEvent() {
		return transaction;
	}
	
	@Override
	public int hashCode() {
		return (int) id.longValue();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		return ((TransactionElement) obj).id.equals(id) &&
				((TransactionElement) obj).getAlias().equals(getAlias());
	}
	
	@Override
	public String toString() {
		return type.toString() + "[" + id + "]";
	}

	public boolean isLinkElement() {
		return getAlias().equals("link");
	}

	public boolean isIgnoreNonExistence() {
		return ignoreNonExistence;
	}

	public void setIgnoreNonExistence(boolean ignoreNonExistence) {
		this.ignoreNonExistence = ignoreNonExistence;
	}
	
}
