package de.protubero.beanstore.init;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanStoreEntity;
import de.protubero.beanstore.store.BeanStoreMetaInfo;
import de.protubero.beanstore.store.BeanStoreReadAccess;
import de.protubero.beanstore.store.EntityReadAccess;
import de.protubero.beanstore.store.Store;
import de.protubero.beanstore.txmanager.BeanStoreCallbacks;
import de.protubero.beanstore.txmanager.TransactionFactory;
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
	public void locked(Consumer<TransactionFactory> consumer) {
		transactionManager.locked(consumer);
	}

	@Override
	public void lockedAsync(Consumer<TransactionFactory> consumer) {
		transactionManager.lockedAsync(consumer);
	}
	
	
	@Override
	public BeanStoreReadAccess read() {
		return new BeanStoreReadAccess() {
			
			@Override
			public BeanStoreReadAccess snapshot() {
				return store.snapshot();
			}
			
			@Override
			public <T extends AbstractEntity> Optional<EntityReadAccess<T>> entityOptional(Class<T> aClass) {
				return store.entity(aClass).map(e -> {
					return new EntityReadAccess<T>() {

						@Override
						public BeanStoreEntity<T> meta() {
							return e;
						}

						@Override
						public T find(Long id) {
							return store.find(aClass, id);
						}

						@Override
						public Optional<T> findOptional(Long id) {
							return store.findOptional(aClass, id);
						}

						@Override
						public Stream<T> stream() {
							return store.stream(aClass);
						}

						@Override
						public EntityReadAccess<T> snapshot() {
							return null;
						}

						@Override
						public int count() {
							return 0;
						}
						
						
					};
				});
			}
			
			@Override
			public <T extends AbstractPersistentObject> Optional<EntityReadAccess<T>> entityOptional(String alias) {
				return null;
			}
			
			
			@Override
			public BeanStoreMetaInfo meta() {
				return BeanStoreImpl.this.meta();
			}
		};
	}

	@Override
	public BeanStoreCallbacks callbacks() {
		return storeWriter;
	}


}
