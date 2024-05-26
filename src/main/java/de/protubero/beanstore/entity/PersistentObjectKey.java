package de.protubero.beanstore.entity;

import java.util.Objects;

/**
 * The <i>complete</i> key of an instance, consisting of the entity alias and the id. 
 *
 */
public interface PersistentObjectKey<T extends AbstractPersistentObject> {

	/**
	 * The entity alias. 
	 */
	String alias();
	
	/**
	 * The instance id. 
	 */
	long id();
	
	Class<T> entityClass();
	
	
	public static PersistentObjectKey<AbstractPersistentObject> of(String alias, long id) {
		Objects.requireNonNull(alias);
		
		return new PersistentObjectKeyImpl<>(null, alias, id);
	}

	public static <T extends AbstractEntity> PersistentObjectKey<T> of(Class<T> entityClass, long id) {
		return new PersistentObjectKeyImpl<>(entityClass, null, id);
	}

	static <T extends AbstractPersistentObject> PersistentObjectKey<T> of(T instance) {
		Objects.requireNonNull(instance);
		Objects.requireNonNull(instance.id());
		
		String alias = instance.alias();
		Objects.requireNonNull(alias);
		
		long id = instance.id(); 
		
		return new PersistentObjectKeyImpl<>(null, alias, id);
	}

	default boolean isKeyOfNewObject() {
		return id() < 0;
	}
	
	
}
