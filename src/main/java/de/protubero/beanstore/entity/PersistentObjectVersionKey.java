package de.protubero.beanstore.entity;

import java.util.Objects;

import de.protubero.beanstore.entity.AbstractPersistentObject.State;

public interface PersistentObjectVersionKey<T extends AbstractPersistentObject> {

	/**
	 * The entity alias. 
	 */
	String alias();
	
	/**
	 * The instance id. 
	 */
	long id();
	
	Class<T> entityClass();	
	
	int version();	
	
	public static PersistentObjectVersionKey<AbstractPersistentObject> of(String alias, long id, int version) {
		Objects.requireNonNull(alias);
		
		return new PersistentObjectVersionKeyImpl<>(null, alias, id, version);
	}

	public static <T extends AbstractEntity> PersistentObjectVersionKey<T> of(Class<T> entityClass, long id, int version) {
		return new PersistentObjectVersionKeyImpl<>(entityClass, null, id, version);
	}

	static <T extends AbstractPersistentObject> PersistentObjectVersionKey<T> of(T instance) {
		if (instance.state() != State.STORED && instance.state() != State.OUTDATED) {
			throw new RuntimeException("Invalid state: " + instance.state());
		}
		Objects.requireNonNull(instance);
		Objects.requireNonNull(instance.id());
		
		String alias = instance.alias();
		Objects.requireNonNull(alias);
		
		long id = instance.id(); 
		int version = instance.version();
		
		return new PersistentObjectVersionKeyImpl<>(null, alias, id, version);
	}
	
	
}
