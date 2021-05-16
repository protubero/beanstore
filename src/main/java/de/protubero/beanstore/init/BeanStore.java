package de.protubero.beanstore.init;

import java.util.function.Consumer;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.BeanChange;
import de.protubero.beanstore.store.ReadableBeanStore;
import de.protubero.beanstore.txmanager.DeferredTransactionExecutionContext;
import de.protubero.beanstore.txmanager.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.writer.BeanStoreChange;

/**
 * A facade to Store (ReadableBeanStore), StoreWriter and TransactionManager 
 * 
 * 
 * @author mscha
 *
 */
public interface BeanStore extends ReadableBeanStore {

	/**
	 * Create a new transaction. 
	 * 
	 * @return a transaction
	 */
	ExecutableBeanStoreTransaction transaction();
	
	void executeDeferred(Consumer<DeferredTransactionExecutionContext> consumer);
	
	
	void verify(Consumer<BeanStoreChange> consumer);	
	
	void verifyInstance(Consumer<BeanChange<?>> consumer);

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void verifyInstance(Class<T> entityClass, Consumer<BeanChange<T>> consumer) {
		verifyInstance(bc -> {
			if (entityClass.isAssignableFrom(bc.entity().entityClass())) {
				consumer.accept((BeanChange<T>) bc);
			}
		});
	}
	
	
	void onChange(Consumer<BeanStoreChange> consumer);

	void onChangeInstance(Consumer<BeanChange<?>> consumer);

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void onChangeInstance(Class<T> entityClass, Consumer<BeanChange<T>> consumer) {
		onChangeInstance(bc -> {
			if (entityClass.isAssignableFrom(bc.entity().entityClass())) {
				consumer.accept((BeanChange<T>) bc);
			}
		});
	}

	
	void onChangeAsync(Consumer<BeanStoreChange> consumer);

	void onChangeInstanceAsync(Consumer<BeanChange<?>> consumer);

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void onChangeInstanceAsync(Class<T> entityClass, Consumer<BeanChange<T>> consumer) {
		onChangeInstanceAsync(bc -> {
			if (entityClass.isAssignableFrom(bc.entity().entityClass())) {
				consumer.accept((BeanChange<T>) bc);
			}
		});
	}
	
	
	void close();
		
	
}
