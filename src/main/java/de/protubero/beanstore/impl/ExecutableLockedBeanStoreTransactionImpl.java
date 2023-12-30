package de.protubero.beanstore.impl;

import java.util.concurrent.CompletableFuture;

import de.protubero.beanstore.api.BeanStoreState;
import de.protubero.beanstore.api.BeanStoreTransactionResult;
import de.protubero.beanstore.api.ExecutableLockedBeanStoreTransaction;
import de.protubero.beanstore.writer.Transaction;

public class ExecutableLockedBeanStoreTransactionImpl extends ExecutableBeanStoreTransactionImpl implements ExecutableLockedBeanStoreTransaction {


	private BeanStoreState lockedStoreState;

	public ExecutableLockedBeanStoreTransactionImpl(Transaction transaction, BeanStoreImpl beanStore) {
		super(transaction, beanStore);
		lockedStoreState = beanStore.state();
	}

	@Override
	public BeanStoreState lockedStoreState() {
		return lockedStoreState;
	}

	@Override
	public CompletableFuture<BeanStoreTransactionResult> execute() {
		return CompletableFuture.completedFuture(beanStore.exec(transaction));
	}
	
	

	
}
