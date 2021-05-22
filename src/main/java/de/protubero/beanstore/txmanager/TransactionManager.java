package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.store.InstanceFactory;
import de.protubero.beanstore.store.BeanStoreReader;
import de.protubero.beanstore.store.Store;
import de.protubero.beanstore.writer.BeanStoreChange;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;

public interface TransactionManager {

	StoreWriter storeWriter();
	

	default ExecutableBeanStoreTransaction transaction() {
		return transaction(null, PersistentTransaction.TRANSACTION_TYPE_DEFAULT);
	}
	
	default ExecutableBeanStoreTransaction transaction(String transactionId, int transactionType) {
		return new ExecutableTransaction(Transaction.of(storeWriter().dataStore(), storeWriter().dataStore(),
				transactionId, transactionType), this);
	}

	
	void executeAsync(Transaction transaction, Consumer<BeanStoreChange> consumer);
		
	BeanStoreChange execute(Transaction transaction);

	void executeDeferred(Consumer<DeferredTransactionExecutionContext> consumer);

	void executeDeferredAsync(Consumer<DeferredTransactionExecutionContext> consumer);
	
	void close();
	
	
}
