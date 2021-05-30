package de.protubero.beanstore.store;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanStoreEntity;

public interface EntityReadAccess<T extends AbstractPersistentObject> extends Iterable<T> {

	BeanStoreEntity<T> meta();

	T find(Long id);
	
	Optional<T> findOptional(Long id);

	/**
	 * Stream all instances 
	 * 
	 * @param <T>
	 * @param entityClass
	 * @return
	 */	
	Stream<T> stream();
	
	EntityReadAccess<T> snapshot();
	
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	int count();
}
