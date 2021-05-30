package de.protubero.beanstore.txmanager;

import java.util.Objects;
import java.util.function.Consumer;

import de.protubero.beanstore.writer.StoreWriter;

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
	
	protected void immediate(Consumer<TransactionFactory> consumer) {
		consumer.accept(new LockedStoreTransactionFactory(storeWriter));
	}
	

}
