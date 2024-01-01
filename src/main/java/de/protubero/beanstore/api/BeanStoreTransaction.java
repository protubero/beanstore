package de.protubero.beanstore.api;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;


/**
 * Adds the explicit handling of bean-based instances to the basic transaction.
 *
 */
public interface BeanStoreTransaction extends BaseTransaction {
	
	/**
	 * Create a BeanStore bean instance of the type determined by the parameter. 
	 */
	<T extends AbstractEntity> T create(Class<T> aClass);

	
	/**
	 * Delete a BeanStore bean instance of the type determined by the parameter and the id. 
	 */
	<T extends AbstractEntity> void delete(Class<T> aClass, long id);
	
	<T extends AbstractPersistentObject> void deleteOptLocked(String alias, long id, int version);
	
	<T extends AbstractEntity> void deleteOptLocked(Class<T> aClass, long id, int version);
	
	<T extends AbstractPersistentObject> void deleteOptLocked(T instance);
	
	<T extends AbstractPersistentObject> T updateOptLocked(T instance);
	
	<T extends AbstractEntity> T updateOptLocked(Class<T> aClass, long id, int version);
	
	<T extends AbstractEntity> T update(Class<T> aClass, long id);

	
}
