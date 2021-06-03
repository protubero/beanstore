package de.protubero.beanstore.api;

import java.util.function.Consumer;

import de.protubero.beanstore.txmanager.TransactionFactory;

/**
 *  
 * The main BeanStore class.
 * 
 *
 */
public interface BeanStore extends BeanStoreTransactionFactory {

	
	BeanStoreMetaInfo meta();
	
	
	/**
	 * Access Store and execute transactions on a locked store.
	 * The method blocks the calling thread until the operation is finished.   
	 * 
	 */
	void locked(Consumer<BeanStoreTransactionFactory> consumer);

	/**
	 * Access Store and execute transactions on a locked store.
	 * The method does NOT block the calling thread.   
	 */
	void lockedAsync(Consumer<BeanStoreTransactionFactory> consumer);
	
	/**
	 * Returns an interface which provides access to all READ operations of the store.
	 * 
	 * @return a BeanStoreReadAccess instance
	 */
	BeanStoreReadAccess read();

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
