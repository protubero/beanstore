package de.protubero.beanstore.pluginapi;

import de.protubero.beanstore.tx.TransactionEvent;

public interface FactoryTransactionListener extends BeanStorePlugin {

	
	/**
	 * When the store initialisation code executes a transaction, you'll be notified here.   
	 */
	default void onInitTransaction(TransactionEvent bsc) {}

	/**
	 * When a store migration transaction is executed, you'll be notified here.   
	 */
	default void onMigrationTransaction(TransactionEvent bsc) {}
		
	
	
}
