package de.protubero.beanstore.api;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.BeanStoreEntity;
import de.protubero.beanstore.base.entity.BeanStoreException;

/**
 * Knows everything about the entities registered at a store. 
 *
 */
public interface BeanStoreMetaInfo extends Iterable<BeanStoreEntity<?>> {

	/**
	 * Returns meta information about the entity with the given alias.
	 * If there is no entity with the given alias, the returned optional object is 'empty'.
	 * 
	 * @param alias the entity alias
	 * @return Meta information. 
	 */
	<T extends AbstractPersistentObject> Optional<BeanStoreEntity<T>> entityOptional(String alias);

	
	/**
	 * Returns meta information about the entity with the given entity class.
	 * If there is no entity with the given entity class, the returned optional object is 'empty'.
	 * 
	 * @param alias the entity alias
	 * @return Meta information. 
	 */
	<T extends AbstractEntity> Optional<BeanStoreEntity<T>> entityOptional(Class<T> entityClass);

	
	/**
	 * Returns entity information of entity with alias <i>alias</i>
	 *  
	 * @throws BeanStoreException if there exists no entity with such an alias 
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> BeanStoreEntity<T> entity(String alias) {
		return (BeanStoreEntity<T>) entityOptional(alias).orElseThrow(() -> {
			throw new BeanStoreException("Sorry, never heard of an entity with alias " + alias);
		});
	}

	/**
	 * Returns entity information of entity class <i>entityClass</i>.<br>
	 * 
	 * @throws BeanStoreException if there exist no entity with such an entity class 
	 */
	default <T extends AbstractEntity> BeanStoreEntity<T> entity(Class<T> entityClass) throws BeanStoreException {
		return entityOptional(entityClass).orElseThrow(() -> {
			throw new BeanStoreException("Sorry, never heard of an entity of entity class " + entityClass);
		});
	}
	
	/**
	 * Stream all entities 
	 */
	Stream<BeanStoreEntity<?>> stream();
	
	/**
	 * Iterate over all entities
	 */
	default Iterator<BeanStoreEntity<?>> iterator() {
		return stream().iterator();
	}
	
	
}
