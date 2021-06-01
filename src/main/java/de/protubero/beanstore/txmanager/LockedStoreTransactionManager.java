package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.init.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;
import de.protubero.beanstore.writer.TransactionEvent;

public class LockedStoreTransactionManager extends AbstractTransactionManager {

	public LockedStoreTransactionManager(StoreWriter storeWriter) {
		super(storeWriter);
	}

	@Override
	public void executeAsync(Transaction transaction, Consumer<TransactionEvent> consumer) {
		execute(transaction);
		if (consumer != null) {
			consumer.accept(transaction);
		}
	}

	@Override
	public TransactionEvent execute(Transaction transaction) {
		storeWriter.execute(transaction);
		return transaction;
	}

	@Override
	public void locked(Consumer<TransactionFactory> consumer) {
		immediate(consumer);
	}

	@Override
	public void close() {
		// NOP
	}

	@Override
	public void lockedAsync(Consumer<TransactionFactory> consumer) {
		immediate(consumer);
	}
	
	public static ExecutableBeanStoreTransaction transaction(StoreWriter writer) {
		return new LockedStoreTransactionManager(writer).transaction();
	}

}
