package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.writer.TransactionEvent;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;

public class ImmediateTransactionManager extends AbstractTransactionManager {

	public ImmediateTransactionManager(StoreWriter storeWriter) {
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
	public void executeDeferred(Consumer<TransactionFactory> consumer) {
		immediate(consumer);
	}

	@Override
	public void close() {
		// NOP
	}

	@Override
	public void executeDeferredAsync(Consumer<TransactionFactory> consumer) {
		immediate(consumer);
	}
	
	public static ExecutableBeanStoreTransaction transaction(StoreWriter writer) {
		return new ImmediateTransactionManager(writer).transaction();
	}

}
