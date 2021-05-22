package de.protubero.beanstore.init;

import java.util.function.Consumer;

import de.protubero.beanstore.store.BeanStoreReader;
import de.protubero.beanstore.txmanager.BeanStoreWriter;
import de.protubero.beanstore.txmanager.DeferredTransactionExecutionContext;
import de.protubero.beanstore.txmanager.ExecutableBeanStoreTransaction;

/**
 *  
 * 
 * 
 * @author mscha
 *
 */
public interface BeanStore {

	/**
	 * Create a new transaction. 
	 * 
	 * @return a transaction
	 */
	ExecutableBeanStoreTransaction transaction();
	
	void executeDeferred(Consumer<DeferredTransactionExecutionContext> consumer);
	
	BeanStoreReader reader();

	BeanStoreWriter writer();

	void close();
	
	
}
