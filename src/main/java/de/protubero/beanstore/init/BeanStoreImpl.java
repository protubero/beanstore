package de.protubero.beanstore.init;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanChange;
import de.protubero.beanstore.base.BeanStoreEntity;
import de.protubero.beanstore.base.InstanceRef;
import de.protubero.beanstore.base.StoreSnapshot;
import de.protubero.beanstore.store.Store;
import de.protubero.beanstore.txmanager.DeferredTransactionExecutionContext;
import de.protubero.beanstore.txmanager.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.txmanager.TransactionManager;
import de.protubero.beanstore.writer.BeanStoreChange;
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
	public void verify(Consumer<BeanStoreChange> consumer) {
		storeWriter.addVerifyTransactionListener(consumer);
	}

	@Override
	public void verifyInstance(Consumer<BeanChange<?>> consumer) {
		storeWriter.addVerifyInstanceTransactionListener(consumer);
	}

	@Override
	public void onChange(Consumer<BeanStoreChange> consumer) {
		storeWriter.addSyncTransactionListener(consumer);
	}

	@Override
	public void onChangeInstance(Consumer<BeanChange<?>> consumer) {
		storeWriter.addSyncInstanceTransactionListener(consumer);
	}

	@Override
	public void onChangeAsync(Consumer<BeanStoreChange> consumer) {
		storeWriter.addAsyncTransactionListener(consumer);
	}

	@Override
	public void onChangeInstanceAsync(Consumer<BeanChange<?>> consumer) {
		storeWriter.addAsyncInstanceTransactionListener(consumer);
	}

	@Override
	public <T extends AbstractPersistentObject> T find(InstanceRef ref) {
		return store.find(ref);
	}

	@Override
	public <T extends AbstractEntity> T find(T ref) {
		return store.find(ref);
	}

	@Override
	public <T extends AbstractPersistentObject> Optional<T> findOptional(InstanceRef ref) {
		return store.findOptional(ref);
	}

	@Override
	public <T extends AbstractPersistentObject> T find(String alias, Long id) {
		return store.find(alias, id);
	}

	@Override
	public <T extends AbstractEntity> T find(Class<T> aClass, Long id) {
		return store.find(aClass, id);
	}

	@Override
	public <T extends AbstractPersistentObject> Optional<T> findOptional(String alias, Long id) {
		return store.findOptional(alias, id);
	}

	@Override
	public <T extends AbstractEntity> Optional<T> findOptional(Class<T> aClass, Long id) {
		return store.findOptional(aClass, id);
	}

	@Override
	public <T extends AbstractPersistentObject> Stream<T> objects(String alias) {
		return store.objects(alias);
	}

	@Override
	public <T extends AbstractEntity> Stream<T> objects(Class<T> aClass) {
		return store.objects(aClass);
	}

	@Override
	public List<AbstractPersistentObject> resolveExisting(Iterable<? extends InstanceRef> refList) {
		return store.resolveExisting(refList);
	}

	@Override
	public List<AbstractPersistentObject> resolve(Iterable<? extends InstanceRef> refList) {
		return store.resolve(refList);
	}


	@Override
	public boolean exists(String alias) {
		return store.exists(alias);
	}


	@Override
	public StoreSnapshot snapshot() {
		return storeWriter.snapshot();
	}

	@Override
	public Optional<BeanStoreEntity<?>> entity(String alias) {
		return store.entity(alias);
	}

	@Override
	public <X extends AbstractEntity> Optional<BeanStoreEntity<X>> entity(Class<X> entityClass) {
		return store.entity(entityClass);
	}

	@Override
	public Collection<BeanStoreEntity<?>> entities() {
		return store.entities();
	}


	@Override
	public void executeDeferred(Consumer<DeferredTransactionExecutionContext> consumer) {
		transactionManager.executeDeferred(consumer);
	}







}
