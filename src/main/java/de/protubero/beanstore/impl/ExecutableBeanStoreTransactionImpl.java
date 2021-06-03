package de.protubero.beanstore.impl;

import java.util.function.Consumer;

import de.protubero.beanstore.api.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.txmanager.ExecutableTransaction;

public class ExecutableBeanStoreTransactionImpl extends BeanStoreTransactionImpl implements ExecutableBeanStoreTransaction {

	private ExecutableTransaction executableTransaction;
	
	ExecutableBeanStoreTransactionImpl(ExecutableTransaction executableTransaction) {
		super(executableTransaction.getTransaction());
		
		this.executableTransaction = executableTransaction;
	}

	@Override
	public void executeAsync(Consumer<TransactionEvent> consumer) {
		executableTransaction.executeAsync(consumer);
	}

	@Override
	public TransactionEvent execute() {
		return executableTransaction.execute();
	}

}
