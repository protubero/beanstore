package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.writer.Transaction;

public class ExecutableTransaction  {

	private TransactionManager transactionManager;
	private Transaction transaction;
	
	public ExecutableTransaction(Transaction transaction, TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		this.transaction = transaction;
	}

	public void executeAsync(Consumer<TransactionEvent> consumer) {
		transactionManager.executeAsync(transaction, consumer);
	}

	public TransactionEvent execute() {
		return transactionManager.execute(transaction);
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public Transaction getTransaction() {
		return transaction;
	}

}
