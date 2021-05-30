package de.protubero.beanstore.init;

import java.util.function.Consumer;

import de.protubero.beanstore.store.BeanStoreMetaInfo;
import de.protubero.beanstore.store.BeanStoreReadAccess;
import de.protubero.beanstore.txmanager.BeanStoreCallbacks;
import de.protubero.beanstore.txmanager.TransactionFactory;

/**
 *  
 * The main BeanStore class.
 * 
 *
 */
public interface BeanStore extends TransactionFactory {

	
	BeanStoreMetaInfo meta();
	
	
	/**
	 * Access Store and execute transactions on a locked store.
	 * The method blocks the calling thread until the operation is finished.   
	 * 
	 */
	void locked(Consumer<TransactionFactory> consumer);

	/**
	 * Access Store and execute transactions on a locked store.
	 * The method does NOT block the calling thread.   
	 */
	void lockedAsync(Consumer<TransactionFactory> consumer);
	
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
