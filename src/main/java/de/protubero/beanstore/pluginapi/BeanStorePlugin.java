package de.protubero.beanstore.pluginapi;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.builder.BeanStoreBuilder;

/**
 * Plugin interface 
 * 
 */
public interface BeanStorePlugin {

	
	/**
	 * The create() method of the bean store builder has just been invoked.  
	 * It is still time to add some configuration to the builder, e.g. 
	 * to register Kryo serializers.
	 */
	default void onStartCreate(BeanStoreBuilder beanStoreBuilder) {}
	
	/**
	 * The persisted transactions has been read from the file and the store is migrated
	 * or initialized. Overwrite this method e.g. to register store callbacks.
	 * The snapshot parameter has the read-only version of the store at this point in time. 
	 * Writes to the bean store will not affect the data of the snapshot.   
	 */
	default void onEndCreate(BeanStore beanStore) {}
	

}
