package de.protubero.beanstore.api;

/**
 * A transaction factory creates transaction.
 * 
 */
public interface BeanStoreTransactionFactory {

	/**
	 * Create a new transaction. 
	 * 
	 * @return a transaction
	 */
	ExecutableBeanStoreTransaction transaction();
	
}
