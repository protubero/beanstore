package de.protubero.beanstore.store;

import java.util.Optional;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.Companion;

public interface EntityStoreSet<E extends EntityStore<?>> extends Iterable<E>, CompanionShip {
		
	boolean isImmutable();
	
	
	<T extends AbstractPersistentObject> EntityStore<T> store(String alias);
	
	<T extends AbstractPersistentObject> EntityStore<T> store(Class<T> aClass);

	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> EntityStore<T> store(T ref) {
		return (EntityStore<T>) store(ref.companion().entityClass());
	}
	
	<T extends AbstractPersistentObject> Optional<EntityStore<T>> storeOptional(String alias);

	<T extends AbstractPersistentObject> Optional<EntityStore<T>> storeOptional(Class<T> aClass);

	boolean empty();
	
	@Override
	default <T extends AbstractPersistentObject> Optional<Companion<T>> companionByClass(Class<T> entityClazz) {
		return storeOptional(entityClazz).map(s -> s.companion());
	}

	@SuppressWarnings("unchecked")
	@Override
	default <T extends AbstractPersistentObject> Optional<Companion<T>> companionByAlias(String alias) {
		return storeOptional(alias).map(s -> (Companion<T>) s.companion());
	}


	EntityStoreSet<E> internalCloneStoreSet();

	
}
