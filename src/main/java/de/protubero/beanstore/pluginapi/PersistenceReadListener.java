package de.protubero.beanstore.pluginapi;

import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.tx.TransactionEvent;

public interface PersistenceReadListener extends BeanStorePlugin {

	/**
	 * A transaction has just been read from the file.<br>
	 * It is technically possible to alter the transaction but don't do that!<br> 
	 * This method is meant for logging and debugging purposes. 
	 */
	void onReadTransaction(PersistentTransaction transaction);
	
	
	
}
