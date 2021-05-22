package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.BeanChange;
import de.protubero.beanstore.writer.BeanStoreChange;

public interface BeanStoreWriter {

	
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
	
	
	
}
