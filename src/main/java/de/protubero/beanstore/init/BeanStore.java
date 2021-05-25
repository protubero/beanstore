package de.protubero.beanstore.init;

import java.util.function.Consumer;

import de.protubero.beanstore.store.BeanStoreReadAccess;
import de.protubero.beanstore.txmanager.BeanStoreCallbacks;
import de.protubero.beanstore.txmanager.TransactionFactory;
import de.protubero.beanstore.txmanager.ExecutableBeanStoreTransaction;

/**
 *  
 * The main BeanStore class.
 * 
 *
 */
public interface BeanStore extends TransactionFactory {

	/**
	 * 
	 * 
	 * @param consumer the 
	 */
	void executeDeferred(Consumer<TransactionFactory> consumer);
	
	/**
	 * Returns an interface which provides access to all READ operations of the store.
	 * 
	 * @return a BeanStoreReader instance
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
