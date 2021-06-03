package de.protubero.beanstore.api;

import java.util.function.Consumer;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.tx.InstanceTransactionEvent;
import de.protubero.beanstore.base.tx.TransactionEvent;

/**
 * An interface to the callback methods of a BeanStore. 
 *
 */
public interface BeanStoreCallbacks {

	/**
	 * Verify a transaction. 
	 */
	void verify(Consumer<TransactionEvent> consumer);	
	
	
	/**
	 * Verify a transaction, one instance at a time. 
	 */
	void verifyInstance(Consumer<InstanceTransactionEvent<?>> consumer);

	/**
	 * Verify only instances of a certain entity
	 * 
	 * @param <T>
	 * @param entityClass
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void verifyInstance(Class<T> entityClass, Consumer<InstanceTransactionEvent<T>> consumer) {
		verifyInstance(bc -> {
			if (entityClass.isAssignableFrom(bc.entity().entityClass())) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}

	/**
	 * Verify only instances of a certain entity
	 * 
	 * @param <T>
	 * @param alias
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> void verifyInstance(String alias, Consumer<InstanceTransactionEvent<T>> consumer) {
		verifyInstance(bc -> {
			if (bc.entity().alias().equals(alias)) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}
	
	/**
	 * Synchronous notification about store transactions. 
	 * 
	 * @param consumer
	 */
	void onChange(Consumer<TransactionEvent> consumer);

	/**
	 * Synchronous notification about store transactions - one instance at a time. 
	 * 
	 * @param consumer
	 */
	void onChangeInstance(Consumer<InstanceTransactionEvent<?>> consumer);

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void onChangeInstance(Class<T> entityClass, Consumer<InstanceTransactionEvent<T>> consumer) {
		onChangeInstance(bc -> {
			if (entityClass.isAssignableFrom(bc.entity().entityClass())) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}

	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> void onChangeInstance(String alias, Consumer<InstanceTransactionEvent<T>> consumer) {
		onChangeInstance(bc -> {
			if (bc.entity().alias().equals(alias)) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}
	
	void onChangeAsync(Consumer<TransactionEvent> consumer);

	void onChangeInstanceAsync(Consumer<InstanceTransactionEvent<?>> consumer);

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void onChangeInstanceAsync(Class<T> entityClass, Consumer<InstanceTransactionEvent<T>> consumer) {
		onChangeInstanceAsync(bc -> {
			if (entityClass.isAssignableFrom(bc.entity().entityClass())) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> void onChangeInstanceAsync(String alias, Consumer<InstanceTransactionEvent<T>> consumer) {
		onChangeInstanceAsync(bc -> {
			if (bc.entity().alias().equals(alias)) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}
	
	
}
