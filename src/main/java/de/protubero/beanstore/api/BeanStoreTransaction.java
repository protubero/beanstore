package de.protubero.beanstore.api;

import de.protubero.beanstore.base.entity.AbstractEntity;

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
	
	
}
