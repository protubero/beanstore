package de.protubero.beanstore.api;

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
	
}
