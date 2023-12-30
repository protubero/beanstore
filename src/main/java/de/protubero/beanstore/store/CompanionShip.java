package de.protubero.beanstore.store;

import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.Companion;

public interface CompanionShip extends Iterable<Companion<?>> {
	
	<T extends AbstractPersistentObject> Optional<Companion<T>> companionByClass(Class<T> entityClazz);
	
	Optional<Companion<? extends AbstractPersistentObject>> companionByAlias(String alias);
	
	Stream<Companion<?>> companions();

	boolean isEmpty();
}
