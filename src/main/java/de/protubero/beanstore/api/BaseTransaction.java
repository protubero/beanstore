package de.protubero.beanstore.api;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;

/**
 * Basic Transaction
 * 
 */
public interface BaseTransaction {

	/**
	 *  Read access to the underlying store
	 */
	BeanStoreReadAccess read();
	
	/**
	 * Create a new instance of an entity with alias <i>alias</i>.<br>
	 *  
	 */
	<T extends AbstractPersistentObject> T create(String alias);

	/**
	 * Use an existing instance (which has not been created by the store) to create a new entity instance 
	 */
	void create(AbstractPersistentObject apo);
	
	
	/**
	 * Update an existing instance. To update any properties, 
	 * just set them on the instance returned by this method. 
	 */
	<T extends AbstractPersistentObject> T update(T instance);

	/**
	 * Delete the instance with the given coordinates. If it does not exist (or has already been deleted), 
	 * the transaction fails. 
	 */
	<T extends AbstractPersistentObject> void delete(String alias, long id);
	
	/**
	 * Delete the given instance. If it has already been deleted, the transaction fails.
	 */
	<T extends AbstractPersistentObject> void delete(T instance);
	
}
