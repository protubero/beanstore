package de.protubero.beanstore.api;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
	 * Find an instance by id. Throws an exception if the id is invalid. 
	 */
	default T find(Integer id) {
		if (id != null) {
			return find(Long.valueOf(id.longValue()));
		} else {
			return find((Long) null);
		}
	}
	
	
	/**
	 * Find an instance by id. 
	 */
	Optional<T> findOptional(Long id);

	/**
	 * Find an instance by id. 
	 */
	default Optional<T> findOptional(Integer id) {
		if (id != null) {
			return findOptional(Long.valueOf(id.longValue()));
		} else {
			return findOptional((Long) null);
		}
	}
	
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
	
	/**
	 * Returns a list with all stored instances 
	 * 
	 */
	default List<T> asList() {
	    return stream().collect(Collectors.toList());
	}

}
