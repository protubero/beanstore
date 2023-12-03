package de.protubero.beanstore.writer;

import java.util.function.Consumer;
import java.util.function.Supplier;

import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;

public interface TransactionStoreContext extends TransactionContext {

	StoreWriter storeWriter();

	Supplier<ImmutableEntityStoreSet> storeSetSupplier();

	void onTransactionExecuted(ImmutableEntityStoreSet source, ImmutableEntityStoreSet target);

	default TransactionEvent execute(Transaction transaction) {
		ImmutableEntityStoreSet currentStoreSet = storeSetSupplier().get();		
		ImmutableEntityStoreSet targetStoreSet = storeWriter().execute(transaction, currentStoreSet);
		onTransactionExecuted(currentStoreSet, targetStoreSet);
		return transaction;
	}

	default void execute(Transaction transaction, Consumer<TransactionEvent> callback) {
		TransactionEvent event = execute(transaction);
		callback.accept(event);
	}
	
	
}
