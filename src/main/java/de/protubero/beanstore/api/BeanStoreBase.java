package de.protubero.beanstore.api;

import de.protubero.beanstore.entity.AbstractEntity;

public interface BeanStoreBase {


	/**
	 * Meta information of the store, i.e. information about the entities in the store. 
	 */
	BeanStoreMetaInfo meta();
	
	/**
	 * Access current persistent state of the store.
	 * 
	 * @return a BeanStoreState instance
	 */
	BeanStoreSnapshot snapshot();
	
	
	/**
	 * Create a new executable transaction. 
	 * 
	 * @return a transaction
	 */
	ExecutableBeanStoreTransaction transaction();
	
	ExecutableBeanStoreTransaction transaction(String description);
	
    default BeanStoreTransactionResult create(AbstractEntity entity) {
    	var tx = transaction();
    	tx.create(entity);
    	return tx.execute();
    }

    default BeanStoreTransactionResult update(AbstractEntity entity) {
    	var tx = transaction();
    	tx.update(entity);
    	return tx.execute();
    }

    default <T extends AbstractEntity> BeanStoreTransactionResult delete(Class<T> entityClass, Long id) {
    	var tx = transaction();
    	tx.delete(entityClass, id);
    	return tx.execute();
    }		
	
     default <T extends AbstractEntity> BeanStoreTransactionResult delete(T entityInstance) {
    	var tx = transaction();
    	tx.delete(entityInstance);
    	return tx.execute();
    }		
}
