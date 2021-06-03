package de.protubero.beanstore.api;

/**
 * A transaction factory creates transactions.
 * 
 * @author Captain Obvious
 */
public interface BeanStoreTransactionFactory {

	/**
	 * Create a new executable transaction. 
	 * 
	 * @return a transaction
	 */
	ExecutableBeanStoreTransaction transaction();
	
}
