package de.protubero.beanstore.txmanager;

import java.util.Objects;

import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;

public class LockedStoreTransactionFactory implements TransactionFactory {

	private StoreWriter storeWriter;
	
	public LockedStoreTransactionFactory(StoreWriter storeWriter) {
		this.storeWriter = Objects.requireNonNull(storeWriter);
	}

	@Override
	public ExecutableBeanStoreTransaction transaction() {
		var tx = Transaction.of(storeWriter.dataStore());
		return new ExecutableTransaction(tx, new LockedStoreTransactionManager(storeWriter));
	}
	
}
