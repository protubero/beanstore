package de.protubero.beanstore.entity;

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
		return PersistentObjectVersionKey.of(entityClass, id, version);
	}
	
	public static PersistentObjectVersionKey<AbstractPersistentObject> versionKey(String alias, long id, int version) {
		return PersistentObjectVersionKey.of(alias, id, version);
	}
	
	public static <T extends AbstractPersistentObject> PersistentObjectVersionKey<T> versionKey(T instance) {
		return PersistentObjectVersionKey.of(instance);
	}

	@SuppressWarnings("unchecked")
	public static <T extends AbstractPersistentObject> PersistentObjectKey<T> key(PersistentObjectVersionKey<T> key) {
		return (PersistentObjectKey<T>) PersistentObjectKey.of(key.alias(), key.id());
	}
	
	
}
