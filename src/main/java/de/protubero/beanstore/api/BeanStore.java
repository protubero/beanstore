package de.protubero.beanstore.api;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *  
 * The main BeanStore class.
 * 
 *
 */
public interface BeanStore {
	

	/**
	 * Meta information of the store, i.e. information about the entities in the store. 
	 */
	BeanStoreMetaInfo meta();
	
	/**
	 * Access current persistent state of the store.
	 * 
	 * @return a BeanStoreState instance
	 */
	BeanStoreState state();
	
	
	/**
	 * Create a new executable transaction. 
	 * 
	 * @return a transaction
	 */
	ExecutableBeanStoreTransaction transaction();
	
	
	/**
	 * Access Store and execute transactions on a locked store.
	 * The method blocks the calling thread until the operation is finished.   
	 * 
	 */
	void locked(Consumer<Supplier<ExecutableLockedBeanStoreTransaction>> consumer);

	/**
	 * Access Store and execute transactions on a locked store.
	 * The method does NOT block the calling thread.   
	 */
	//void lockedAsync(Consumer<ExecutableLockedBeanStoreTransaction> consumer);
	
	/**
	 * Returns an interface which provides access to the store callbacks.
	 * 
	 * @return a BeanStoreCallbacks instance
	 */
	BeanStoreCallbacks callbacks();

	/**
	 * Close the BeanStore.  
	 * 
	 * <p>
	 * First the transaction queue is closed and will not accept new entries.
	 * When the remaining transactions were all executed, the persistent transaction writer 
	 * is flushed and closed as well.
	 * </p>
	 * 
	 * Closing the store prevents further changes but the store is still readable.
	 * 
	 */
	void close();
	
	
}
