package de.protubero.beanstore.txmanager;

import java.util.Objects;

import de.protubero.beanstore.writer.Transaction;
import de.protubero.beanstore.writer.TransactionStoreContext;

public class LockedStoreTransactionFactory implements TransactionFactory {

	private TransactionStoreContext context;
	
	public LockedStoreTransactionFactory(TransactionStoreContext context) {
		this.context = Objects.requireNonNull(context);
	}

	@Override
	public ExecutableTransaction transaction() {
		var tx = Transaction.of(context);
		return new ExecutableTransaction(tx, new LockedStoreTransactionManager(context));
	}
	
}
