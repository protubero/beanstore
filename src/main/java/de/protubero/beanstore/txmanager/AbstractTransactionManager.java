package de.protubero.beanstore.txmanager;

import java.util.Objects;
import java.util.function.Consumer;

import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;

public abstract class AbstractTransactionManager implements TransactionManager {

	/**
	 * 
	 */
	protected StoreWriter storeWriter;

	public AbstractTransactionManager(StoreWriter storeWriter) {
		this.storeWriter = Objects.requireNonNull(storeWriter);
	}
	
	public StoreWriter storeWriter() {
		return storeWriter;
	}
	
	protected void immediate(Consumer<DeferredTransactionExecutionContext> consumer) {
		consumer.accept(new DeferredTransactionExecutionContext() {
			
			@Override
			public ExecutableBeanStoreTransaction transaction() {
				var tx = Transaction.of(storeWriter.dataStore());
				return new ExecutableTransaction(tx, new ImmediateTransactionManager(storeWriter()));
			}
		});
	}
	

}
