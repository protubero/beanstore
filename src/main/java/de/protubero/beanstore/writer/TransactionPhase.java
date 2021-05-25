package de.protubero.beanstore.writer;

/**
 * Phases of a BeanStore transaction. 
 * 
 */
public enum TransactionPhase {
	/**
	 * A fresh transaction, not yet comitted
	 */
	INITIAL, 
	
	/**
	 * First phase when the transaction was committed: Ask verification listeners to 'approve' the transaction.
	 * I.e. to not throw a transaction...
	 */
	VERIFICATION,
	
	/**
	 * After successful verification the transaction is persisted.
	 */
	PERSIST, 
	
	/**
	 * Then the changes represented by the transaction are applied to the store objects.
	 */
	EXECUTE,
	
	/**
	 * Notify all synchronous listeners before the next transaction is pulled from the transaction queue.  
	 */
	COMMITTED_SYNC, 
	
	/**
	 * Notify all asynchronous listeners 
	 */
	COMMITTED_ASYNC  
}