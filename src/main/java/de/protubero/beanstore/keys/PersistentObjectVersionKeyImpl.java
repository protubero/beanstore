package de.protubero.beanstore.keys;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;

public class PersistentObjectVersionKeyImpl<T extends AbstractPersistentObject> implements PersistentObjectVersionKey<T> {

	private String alias;
	private long id;
	private Class<T> entityClass;
	private int version;
	
	PersistentObjectVersionKeyImpl(Class<T> entityClass, String alias, long id, int version) {
		if (entityClass != null && !AbstractEntity.class.isAssignableFrom(entityClass)) {
			throw new RuntimeException("Not an entity class: " + entityClass);
		}
		if (alias == null && entityClass == null) {
			throw new AssertionError(); 
		}
		if (alias != null && entityClass != null) {
			throw new AssertionError(); 
		}
		
		this.alias = alias;
		this.id = id;
		this.entityClass = entityClass;
		this.version = version;
	}
	
	@Override
	public String alias() {
		return alias;
	}

	@Override
	public long id() {
		return id;
	}

	@Override
	public Class<T> entityClass() {
		return entityClass;
	}

	@Override
	public int version() {
		return version;
	}
	
	@Override
	public String toString() {
		return alias() + "#" + id() + " (" + version + ")";
	}

	
	
}
