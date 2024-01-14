package de.protubero.beanstore.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * An executable transaction is explicitly executed by client code, 
 * in contrast to e.g. migration transactions.
 */
public interface ExecutableBeanStoreTransaction extends BeanStoreTransaction {

	/**
	 * Execute the transaction.
	 * The calling thread is blocked until the execution finished.
	 * 
	 * @return Information about the executed transaction, e.g. if it was successful.
	 */
	//BeanStoreTransactionResult execute();

	CompletableFuture<BeanStoreTransactionResult> executeAsync();
	
	default BeanStoreTransactionResult execute() {
		try {
			return executeAsync().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
