package de.protubero.beanstore.impl;

import java.util.concurrent.CompletableFuture;

import de.protubero.beanstore.api.BeanStoreTransactionResult;
import de.protubero.beanstore.tx.Transaction;

public class ExecutableLockedBeanStoreTransactionImpl extends ExecutableBeanStoreTransactionImpl  {



	public ExecutableLockedBeanStoreTransactionImpl(Transaction transaction, BeanStoreImpl beanStore) {
		super(transaction, beanStore);
	}


	/**
	 * In locked state, the transaction circumvents the task queue. We have to ensure  
	 * that the transactions are executed sequentially nevertheless. Thus the 'synchronized' modifier. 
	 */
	@Override
	public synchronized CompletableFuture<BeanStoreTransactionResult> executeAsync() {
		return CompletableFuture.completedFuture(beanStore.exec(transaction));
	}
	
	

	
}
