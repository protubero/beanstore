package de.protubero.beanstore.impl;

import java.util.concurrent.CompletableFuture;

import de.protubero.beanstore.api.BeanStoreSnapshot;
import de.protubero.beanstore.api.BeanStoreTransactionResult;
import de.protubero.beanstore.api.ExecutableLockedBeanStoreTransaction;
import de.protubero.beanstore.tx.Transaction;

public class ExecutableLockedBeanStoreTransactionImpl extends ExecutableBeanStoreTransactionImpl implements ExecutableLockedBeanStoreTransaction {


	private BeanStoreSnapshot lockedStoreState;

	public ExecutableLockedBeanStoreTransactionImpl(Transaction transaction, BeanStoreImpl beanStore) {
		super(transaction, beanStore);
		lockedStoreState = beanStore.snapshot();
	}

	@Override
	public BeanStoreSnapshot lockedStoreState() {
		return lockedStoreState;
	}

	@Override
	public CompletableFuture<BeanStoreTransactionResult> executeAsync() {
		return CompletableFuture.completedFuture(beanStore.exec(transaction));
	}
	
	

	
}
