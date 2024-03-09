package de.protubero.beanstore.api;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.entity.PersistentObjectKey;

/**
 * Read operations related to a single entity.
 */
public interface EntityStoreSnapshot<T extends AbstractPersistentObject> extends Iterable<T> {

	/**
	 * Meta information about the entity.
	 */
	BeanStoreEntity<T> meta();

	/**
	 * Find an instance by id. Returns null if no object with that id exists.
	 */
	T find(long id);

	/**
	 * Find an instance by integer id. Throws an exception if the id is invalid.
	 */
	default T find(int id) {
		long longId = id;
		return find(longId);
	}

	/**
	 * Find an instance by id.
	 */
	default Optional<T> findOptional(long id) {
		return Optional.ofNullable(find(id));
	}

	/**
	 * Find an instance by id.
	 */
	default Optional<T> findOptional(int id) {
		long longId = id;
		return findOptional(longId);
	}

	default T find(PersistentObjectKey<?> key) {
		Objects.requireNonNull(key);

		if (key.alias() != null && !meta().alias().equals(key.alias())) {
			throw new RuntimeException("Invalid alias: " + key.alias());
		}
		if (key.entityClass() != null && (!meta().isBean() || key.entityClass() != meta().entityClass())) {
			throw new RuntimeException("Invalid entity class: " + key.entityClass());
		}
		return find(key.id());
	}

	default Optional<T> findOptional(PersistentObjectKey<?> key) {
		return Optional.ofNullable(find(key));
	}
	
	
	/**
	 * Stream all instances
	 * 
	 */
	Stream<T> stream();

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
