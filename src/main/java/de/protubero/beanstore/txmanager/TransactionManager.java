package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.init.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.init.ExecutableTransaction;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;
import de.protubero.beanstore.writer.TransactionEvent;

public interface TransactionManager {

	StoreWriter storeWriter();
	

	default ExecutableBeanStoreTransaction transaction() {
		return transaction(null, PersistentTransaction.TRANSACTION_TYPE_DEFAULT);
	}
	
	default ExecutableBeanStoreTransaction transaction(String transactionId, int transactionType) {
		return new ExecutableTransaction(Transaction.of(storeWriter().dataStore(), storeWriter().dataStore(),
				transactionId, transactionType), this);
	}

	
	void executeAsync(Transaction transaction, Consumer<TransactionEvent> consumer);
		
	TransactionEvent execute(Transaction transaction);

	void locked(Consumer<TransactionFactory> consumer);

	void lockedAsync(Consumer<TransactionFactory> consumer);
	
	void close();
	
	
}
