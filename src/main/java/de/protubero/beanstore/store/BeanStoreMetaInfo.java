package de.protubero.beanstore.store;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanStoreEntity;

public interface BeanStoreMetaInfo extends Iterable<BeanStoreEntity<?>> {

	/**
	 * Returns meta information about the entity with the given alias.
	 * If there is no entity with the given alias, the returned optional object is 'empty'.
	 * 
	 * @param alias the entity alias
	 * @return Meta information. 
	 */
	<T extends AbstractPersistentObject> Optional<BeanStoreEntity<T>> entityOptional(String alias);

	<T extends AbstractEntity> Optional<BeanStoreEntity<T>> entityOptional(Class<T> entityClass);

	
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> BeanStoreEntity<T> entity(String alias) {
		return (BeanStoreEntity<T>) entityOptional(alias).orElseThrow(() -> {
			throw new StoreException("");
		});
	}

	default <T extends AbstractEntity> BeanStoreEntity<T> entity(Class<T> entityClass) {
		return entityOptional(entityClass).orElseThrow(() -> {
			throw new StoreException("");
		});
	}
	
	Stream<BeanStoreEntity<?>> stream();
	
	default Iterator<BeanStoreEntity<?>> iterator() {
		return stream().iterator();
	}
	
	
}
