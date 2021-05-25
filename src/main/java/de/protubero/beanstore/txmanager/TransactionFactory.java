package de.protubero.beanstore.txmanager;

/**
 * 
 *   
 *
 */
public interface TransactionFactory {

	/**
	 * Create a new transaction. 
	 * 
	 * @return a transaction
	 */
	ExecutableBeanStoreTransaction transaction();
	
}
