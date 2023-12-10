package de.protubero.beanstore.impl;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import de.protubero.beanstore.api.BeanStoreTransactionResult;
import de.protubero.beanstore.api.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.writer.Transaction;

public class ExecutableBeanStoreTransactionImpl extends BeanStoreTransactionImpl implements ExecutableBeanStoreTransaction {

	protected BeanStoreImpl beanStore;
	
	ExecutableBeanStoreTransactionImpl(Transaction transaction, BeanStoreImpl beanStore) {		
		super(transaction);
		
		this.beanStore = Objects.requireNonNull(beanStore);
	}

	@Override
	public CompletableFuture<BeanStoreTransactionResult> execute() {
		return beanStore.execute(transaction);
	}

}
