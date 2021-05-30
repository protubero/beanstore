package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.writer.TransactionEvent;
import de.protubero.beanstore.writer.BeanStoreTransaction;

/**
 * 
 * 
 *
 */
public interface ExecutableBeanStoreTransaction extends BeanStoreTransaction {

	default void executeAsync() {
		executeAsync(null);
	}

	void executeAsync(Consumer<TransactionEvent> consumer);

	/**
	 * Execute the transaction.
	 * The calling thread is blocked until the execution finished.
	 * 
	 * @return Information about the executed transaction, e.g. if it was successful.
	 */
	TransactionEvent execute();
	
	
}
