package de.protubero.beanstore.txmanager;

import de.protubero.beanstore.init.ExecutableBeanStoreTransaction;

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
