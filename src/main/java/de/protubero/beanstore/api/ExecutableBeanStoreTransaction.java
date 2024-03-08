package de.protubero.beanstore.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import de.protubero.beanstore.tx.TransactionFailure;

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

	default BeanStoreTransactionResult executeNonThrowing() {
		try {
			return executeAsync().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	
	default BeanStoreTransactionResult execute() throws TransactionFailure {
		try {
			BeanStoreTransactionResult result = executeAsync().get();
			if (result.failed()) {
				throw result.exception();
			}
			return result;
		} catch (InterruptedException ie) {
			throw new RuntimeException(ie);
		} catch (ExecutionException ee) {
			if (ee.getCause() instanceof TransactionFailure) {
				throw (TransactionFailure) ee.getCause();
			} else {
				throw new RuntimeException(ee.getCause());
			}	
		}
	}
}
