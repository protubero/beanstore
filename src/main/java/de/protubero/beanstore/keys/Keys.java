package de.protubero.beanstore.keys;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;

public final class Keys {

	public Keys() {}
	
	public static <T extends AbstractEntity> PersistentObjectKey<T> key(Class<T> entityClass, long id) {
		return PersistentObjectKey.of(entityClass, id);
	}

	public static PersistentObjectKey<AbstractPersistentObject> key(String alias, long id) {
		return PersistentObjectKey.of(alias, id);
	}
	
	public static <T extends AbstractPersistentObject> PersistentObjectKey<T> key(T instance) {
		return PersistentObjectKey.of(instance);
	}
	
	public static <T extends AbstractEntity> PersistentObjectVersionKey<T> versionKey(Class<T> entityClass, long id, int version) {
		return null;
	}
	
	public static PersistentObjectVersionKey<AbstractPersistentObject> versionKey(String alias, long id, int version) {
		return null;
	}
	
	public static <T extends AbstractPersistentObject> PersistentObjectVersionKey<T> versionKey(T instance) {
		return null;
	}
	
	
}
