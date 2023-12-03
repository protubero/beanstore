package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.writer.Transaction;
import de.protubero.beanstore.writer.TransactionStoreContext;

public interface TransactionManager {

	TransactionStoreContext context();

	default ExecutableTransaction transaction() {
		return transaction(null, PersistentTransaction.TRANSACTION_TYPE_DEFAULT);
	}
	
	default ExecutableTransaction transaction(String transactionId, int transactionType) {
		return new ExecutableTransaction(Transaction.of(context().companionSet(),
				transactionId, transactionType), this);
	}

	void executeAsync(Transaction transaction, Consumer<TransactionEvent> consumer);
		
	TransactionEvent execute(Transaction transaction);

	void locked(Consumer<TransactionFactory> consumer);

	void lockedAsync(Consumer<TransactionFactory> consumer);
	
	void close();
		
	
}
