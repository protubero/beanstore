package de.protubero.beanstore.pluginapi;

import de.protubero.beanstore.persistence.api.PersistentTransaction;

public interface PersistenceWriteListener extends BeanStorePlugin {

	/**
	 * A transaction has just been written to the file.<br> 
	 * It is technically possible to alter the transaction but don't do that!<br>
	 * This method is meant for logging and debugging purposes. 
	 */
	void onWriteTransaction(PersistentTransaction transaction);
	
	
	
}
