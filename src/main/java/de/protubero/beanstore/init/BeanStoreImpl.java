package de.protubero.beanstore.init;

import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.store.BeanStoreMetaInfo;
import de.protubero.beanstore.store.BeanStoreReadAccess;
import de.protubero.beanstore.store.Store;
import de.protubero.beanstore.txmanager.BeanStoreCallbacks;
import de.protubero.beanstore.txmanager.TransactionFactory;
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
	public void locked(Consumer<TransactionFactory> consumer) {
		transactionManager.locked(consumer);
	}

	@Override
	public void lockedAsync(Consumer<TransactionFactory> consumer) {
		transactionManager.lockedAsync(consumer);
	}
	
	@Override
	public BeanStoreReadAccess read() {
		return new BeanStoreReadAccessImpl(store);
	}

	@Override
	public BeanStoreCallbacks callbacks() {
		return storeWriter;
	}

	@Override
	public BeanStoreMetaInfo meta() {
		return new BeanStoreMetaInfoImpl(store);
	}


}
