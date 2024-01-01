package de.protubero.beanstore.pluginapi;

import java.io.File;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.factory.BeanStoreFactory;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.tx.TransactionEvent;

/**
 * Plugin interface 
 * 
 */
public interface BeanStorePlugin {

	
	/**
	 * Notifies the plugin about the file containing the persisted transactions 
	 */
	default void onOpenFile(File file) {}
	

	/**
	 * The create() method of the bean store factory has just been invoked.  
	 * It is still time to add some configuration to the factory, e.g. 
	 * to register Kryo serializers.
	 */
	default void onStartCreate(BeanStoreFactory beanStoreFactory) {}
	
	/**
	 * The persisted transactions has been read from the file and the store is migrated
	 * or initialized. Overwrite this method e.g. to register store callbacks.
	 * The snapshot parameter has the read-only version of the store at this point in time. 
	 * Writes to the bean store will not affect the data of the snapshot.   
	 */
	default void onEndCreate(BeanStore beanStore) {}
	

}
