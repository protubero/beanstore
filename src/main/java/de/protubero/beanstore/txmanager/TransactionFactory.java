package de.protubero.beanstore.txmanager;

/**
 * A transaction factory creates transaction.
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
