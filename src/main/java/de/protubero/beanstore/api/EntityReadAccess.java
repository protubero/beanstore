package de.protubero.beanstore.api;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.BeanStoreEntity;

/**
 * Read operations related to a single entity. 
 */
public interface EntityReadAccess<T extends AbstractPersistentObject> extends Iterable<T> {

	/**
	 * Meta information about the entity. 
	 */
	BeanStoreEntity<T> meta();
	
	/**
	 * Find an instance by id. Throws an exception if the id is invalid. 
	 */
	T find(Long id);
	
	/**
	 * Find an instance by id. 
	 */
	Optional<T> findOptional(Long id);

	/**
	 * Stream all instances 
	 * 
	 */	
	Stream<T> stream();

	/**
	 * Create a read-only copy (a.k.a. snapshot) of this entity store
	 */
	EntityReadAccess<T> snapshot();

	/**
	 * Iterate over all entity instances
	 */
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	/**
	 * Returns the number of stored instances. 
	 */
	int count();
}
