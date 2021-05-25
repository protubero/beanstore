package de.protubero.beanstore.base;

/**
 * 
 *
 * @param <T> The type of the entity instances
 */
public interface InstanceTransactionEvent<T extends AbstractPersistentObject> {

	/**
	 * Possible instance event types.  
	 *
	 */
	public static enum InstanceEventType {
		Create,
		Update,
		Delete
	}

	/**
	 * The event type. 
	 */
	InstanceEventType type();
	
	/**
	 * The instance id. 
	 */
	Long instanceId();
	
	/**
	 * The original instance, before the transaction is applied. Returns NULL for InstanceEnumType.Create events. 
	 */
	T replacedInstance();

	/**
	 * The new instance, either completely new or cloned from the original instance and then updated. 
	 */
	T newInstance();

	/**
	 *  
	 */
	InstancePropertyValue[] values();

	/**
	 * Gives you some meta information about the 'type' of the instance, entity
	 */
	BeanStoreEntity<T> entity();
	
	
}
