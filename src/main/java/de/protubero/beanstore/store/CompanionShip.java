package de.protubero.beanstore.store;

import java.util.Optional;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.Companion;

public interface CompanionShip {
	
	<T extends AbstractPersistentObject> Optional<Companion<T>> companionByClass(Class<T> entityClazz);
	
	<T extends AbstractPersistentObject> Optional<Companion<T>> companionByAlias(String alias);
	
	
}
