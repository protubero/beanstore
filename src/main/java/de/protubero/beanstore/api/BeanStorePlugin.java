package de.protubero.beanstore.api;

import java.io.File;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.persistence.base.PersistentTransaction;

/**
 * Plugin interface 
 * 
 * @author Captain Obvious
 */
public interface BeanStorePlugin {

	
	/**
	 * Notifies the plugin about the file containing the persisted transactions 
	 */
	default void onOpenFile(File file) {
		
	}
	
	/**
	 * Implement this method to validate JavaBean instances which are created 
	 * immediately after the migration process has finished.  
	 */
	default void validate(AbstractPersistentObject apo) {
		
	}

	/**
	 * The create() method of the bean store factory has just been invoked.  
	 * It is still time to add some configuration to the factory.
	 */
	default void onStartCreate(BeanStoreFactory beanStoreFactory) {
		
	}
	
	/**
	 * The persisted transactions has been read from the file and the store is migrated
	 * or initialized. Overwrite this method e.g. to register store callbacks.
	 * The snapshot parameter has the read-only version of the store at this point in time. 
	 * Writes to the bean store will not affect the data of the snapshot.   
	 */
	default void onEndCreate(BeanStore beanStore) {
		
	}
	
	/**
	 * A transaction has just been read from the file.<br>
	 * It is technically possible to alter the transaction but don't do that!<br> 
	 * This method is meant for logging and debugging purposes. 
	 */
	default void onReadTransaction(PersistentTransaction transaction) {
		
	}
	
	/**
	 * A transaction has just been written to the file.<br> 
	 * It is technically possible to alter the transaction but don't do that!<br>
	 * This method is meant for logging and debugging purposes. 
	 */
	default void onWriteTransaction(PersistentTransaction transaction) {
		
	}

	/**
	 * When the store initialisation code executes a transaction, you'll be notified here.   
	 */
	default void onInitTransaction(TransactionEvent bsc) {
		
	}

	/**
	 * When a store migration transaction is executed, you'll be notified here.   
	 */
	default void onMigrationTransaction(TransactionEvent bsc) {
		
	}
		

}
