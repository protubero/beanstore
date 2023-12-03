package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.writer.Transaction;
import de.protubero.beanstore.writer.TransactionStoreContext;

public class LockedStoreTransactionManager extends AbstractTransactionManager {

	
	public LockedStoreTransactionManager(TransactionStoreContext context) {
		super(context);
	}

	@Override
	public void executeAsync(Transaction transaction, Consumer<TransactionEvent> consumer) {
		execute(transaction);
		if (consumer != null) {
			consumer.accept(transaction);
		}
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

	@Override
	public TransactionEvent execute(Transaction transaction) {
		return context.execute(transaction);
	}


}
