package de.protubero.beanstore.api;

import java.util.function.Consumer;

import de.protubero.beanstore.base.tx.TransactionEvent;

/**
 * An executable transaction is explicitly executed by client code, 
 * in contrast to e.g. migration transactions.
 */
public interface ExecutableBeanStoreTransaction extends BeanStoreTransaction {

	/**
	 * Enqueue the transaction. The calling thread immediately returns. 
	 */
	default void executeAsync() {
		executeAsync(null);
	}

	/**
	 * Enqueue the transaction. The calling thread immediately returns. 
	 * After the transaction has been executed, the callback code is invoked.  
	 */
	void executeAsync(Consumer<TransactionEvent> consumer);

	/**
	 * Execute the transaction.
	 * The calling thread is blocked until the execution finished.
	 * 
	 * @return Information about the executed transaction, e.g. if it was successful.
	 */
	TransactionEvent execute();

	
}
