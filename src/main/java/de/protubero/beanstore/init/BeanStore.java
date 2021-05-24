package de.protubero.beanstore.init;

import java.util.function.Consumer;

import de.protubero.beanstore.store.BeanStoreReader;
import de.protubero.beanstore.txmanager.BeanStoreCallbacks;
import de.protubero.beanstore.txmanager.DeferredTransactionExecutionContext;
import de.protubero.beanstore.txmanager.ExecutableBeanStoreTransaction;

/**
 *  
 * The main BeanStore class.
 * 
 *
 */
public interface BeanStore {

	/**
	 * Create a new transaction. 
	 * 
	 * @return a transaction
	 */
	ExecutableBeanStoreTransaction transaction();

	/**
	 * 
	 * 
	 * @param consumer
	 */
	void executeDeferred(Consumer<DeferredTransactionExecutionContext> consumer);
	
	/**
	 * Returns an interface which provides access to all READ operations.
	 * 
	 * @return a BeanStoreReader instance
	 */
	BeanStoreReader reader();

	/**
	 * 
	 * @return a BeanStoreCallbacks instance
	 */
	BeanStoreCallbacks callbacks();

	/**
	 * Close the BeanStore.  
	 * 
	 * <p>
	 * First the transaction queue is closed and will not accept new entries.
	 * Then the transaction writer is flushed and closed as well.
	 * </p>
	 * 
	 * Closing the store prevents further changes but the store is still readable.
	 * 
	 */
	void close();
	
	
}
