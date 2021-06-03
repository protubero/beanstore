package de.protubero.beanstore.impl;

import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreCallbacks;
import de.protubero.beanstore.api.BeanStoreMetaInfo;
import de.protubero.beanstore.api.BeanStoreReadAccess;
import de.protubero.beanstore.api.BeanStoreTransactionFactory;
import de.protubero.beanstore.api.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.store.Store;
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
		return new ExecutableBeanStoreTransactionImpl(transactionManager.transaction());
	}
		
	@Override
	public void locked(Consumer<BeanStoreTransactionFactory> consumer) {
		transactionManager.locked(txFactory -> {
			consumer.accept(new BeanStoreTransactionFactory() {

				@Override
				public ExecutableBeanStoreTransaction transaction() {
					return new ExecutableBeanStoreTransactionImpl(txFactory.transaction());
				}
				
			});	
		});
	}

	@Override
	public void lockedAsync(Consumer<BeanStoreTransactionFactory> consumer) {
		transactionManager.lockedAsync(txFactory -> {
			consumer.accept(new BeanStoreTransactionFactory() {

				@Override
				public ExecutableBeanStoreTransaction transaction() {
					return new ExecutableBeanStoreTransactionImpl(txFactory.transaction());
				}
				
			});	
		});
	}
	
	@Override
	public BeanStoreReadAccess read() {
		return new BeanStoreReadAccessImpl(store);
	}

	@Override
	public BeanStoreCallbacks callbacks() {
		return new BeanStoreCallbacksImpl(storeWriter);
	}

	@Override
	public BeanStoreMetaInfo meta() {
		return new BeanStoreMetaInfoImpl(store);
	}


}
