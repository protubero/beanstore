package de.protubero.beanstore.store;

import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.entity.PersistentObjectVersionKey;

public interface CompanionSet extends Iterable<Companion<?>> {
	
	<T extends AbstractPersistentObject> Optional<Companion<T>> companionByClass(Class<T> entityClazz);
	
	Optional<Companion<? extends AbstractPersistentObject>> companionByAlias(String alias);
	
	Stream<Companion<?>> companions();

	boolean isEmpty();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default <T extends AbstractPersistentObject> Optional<Companion<T>> companionByKey(PersistentObjectKey<T> key) {
		if (key.entityClass() != null) {
			return (companionByClass(key.entityClass()));
		} else {
			return ((Optional) companionByAlias(key.alias()));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default <T extends AbstractPersistentObject> Optional<Companion<T>> companionByKey(PersistentObjectVersionKey<T> key) {
		if (key.entityClass() != null) {
			return (companionByClass(key.entityClass()));
		} else {
			return ((Optional) companionByAlias(key.alias()));
		}
	}
	
}
