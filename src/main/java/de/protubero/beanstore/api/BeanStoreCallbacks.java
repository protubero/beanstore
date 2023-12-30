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
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void verifyInstance(Class<T> entityClass, Consumer<InstanceTransactionEvent<T>> consumer) {
		verifyInstance(bc -> {
			if (bc.newInstance() != null && entityClass.isAssignableFrom(bc.entity().entityClass())) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}

	/**
	 * Verify only instances of a certain entity
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> void verifyInstance(String alias, Consumer<InstanceTransactionEvent<T>> consumer) {
		verifyInstance(bc -> {
			if (bc.newInstance() != null && bc.entity().alias().equals(alias)) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}
	
	/**
	 * Synchronous notification about store transactions. 
	 */
	void onChange(Consumer<TransactionEvent> consumer);

	/**
	 * Synchronous notification about store transactions - one instance at a time. 
	 * 
	 */
	void onChangeInstance(Consumer<InstanceTransactionEvent<?>> consumer);

	/**
	 * Synchronous notification about store transactions - one instance at a time.<br>
	 * Only instances of the entity determined by the <i>entityClass</i> parameter are handled.
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void onChangeInstance(Class<T> entityClass, Consumer<InstanceTransactionEvent<T>> consumer) {
		onChangeInstance(bc -> {
			if (entityClass.isAssignableFrom(bc.entity().entityClass())) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}

	/**
	 * Synchronous notification about store transactions - one instance at a time.<br>
	 * Only instances of the entity determined by the <i>alias</i> parameter are handled.
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> void onChangeInstance(String alias, Consumer<InstanceTransactionEvent<T>> consumer) {
		onChangeInstance(bc -> {
			if (bc.entity().alias().equals(alias)) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}
	
	/**
	 * Asynchronous notification about store transactions. 
	 */
	void onChangeAsync(Consumer<TransactionEvent> consumer);

	/**
	 * Asynchronous notification about store transactions - one instance at a time. 
	 */
	void onChangeInstanceAsync(Consumer<InstanceTransactionEvent<?>> consumer);

	/**
	 * Asynchronous notification about store transactions - one instance at a time.<br>
	 * Only instances of the entity determined by the <i>entityClass</i> parameter are handled.
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void onChangeInstanceAsync(Class<T> entityClass, Consumer<InstanceTransactionEvent<T>> consumer) {
		onChangeInstanceAsync(bc -> {
			if (entityClass.isAssignableFrom(bc.entity().entityClass())) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}
	
	/**
	 * Asynchronous notification about store transactions - one instance at a time.<br>
	 * Only instances of the entity determined by the <i>alias</i> parameter are handled.
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> void onChangeInstanceAsync(String alias, Consumer<InstanceTransactionEvent<T>> consumer) {
		onChangeInstanceAsync(bc -> {
			if (bc.entity().alias().equals(alias)) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}
	
	
}
