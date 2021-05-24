package de.protubero.beanstore.init;

import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.store.BeanStoreReader;
import de.protubero.beanstore.store.Store;
import de.protubero.beanstore.txmanager.BeanStoreCallbacks;
import de.protubero.beanstore.txmanager.DeferredTransactionExecutionContext;
import de.protubero.beanstore.txmanager.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.txmanager.TransactionManager;
import de.protubero.beanstore.writer.StoreWriter;

class BeanStoreImpl implements BeanStore {

	public static final Logger log = LoggerFactory.getLogger(BeanStoreImpl.class);

	private Store store;
	private StoreWriter storeWriter;
	
	private Runnable onCloseCallback;
	private TransactionManager transactionManager;
		
	BeanStoreImpl(TransactionManager transactionManager, Runnable onCloseCallback) {
		this.transactionManager = Objects.requireNonNull(transactionManager);
		this.onCloseCallback = Objects.requireNonNull(onCloseCallback);

		this.storeWriter = transactionManager.storeWriter();		
		this.store = storeWriter.dataStore();
	}

	@Override
	public void close() {
		transactionManager.close();
		onCloseCallback.run();
	}
	
	@Override
	public ExecutableBeanStoreTransaction transaction() {
		return transactionManager.transaction();
	}
		
	@Override
	public void executeDeferred(Consumer<DeferredTransactionExecutionContext> consumer) {
		transactionManager.executeDeferred(consumer);
	}

	@Override
	public BeanStoreReader reader() {
		return store;
	}

	@Override
	public BeanStoreCallbacks callbacks() {
		return storeWriter;
	}


}
