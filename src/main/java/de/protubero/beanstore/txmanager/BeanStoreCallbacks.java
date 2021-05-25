package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.InstanceTransactionEvent;
import de.protubero.beanstore.writer.TransactionEvent;

/**
 * An interface to all callback methods of a BeanStore. 
 * 
 *
 */
public interface BeanStoreCallbacks {

	/**
	 * Verify a transaction. 
	 */
	void verify(Consumer<TransactionEvent> consumer);	
	
	void verifyInstance(Consumer<InstanceTransactionEvent<?>> consumer);

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void verifyInstance(Class<T> entityClass, Consumer<InstanceTransactionEvent<T>> consumer) {
		verifyInstance(bc -> {
			if (entityClass.isAssignableFrom(bc.entity().entityClass())) {
				consumer.accept((InstanceTransactionEvent<T>) bc);
			}
		});
	}
	
	
	void onChange(Consumer<TransactionEvent> consumer);

	void onChangeInstance(Consumer<InstanceTransactionEvent<?>> consumer);

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void onChangeInstance(Class<T> entityClass, Consumer<InstanceTransactionEvent<T>> consumer) {
		onChangeInstance(bc -> {
			if (entityClass.isAssignableFrom(bc.entity().entityClass())) {
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
	
	
	
}
