package de.protubero.beanstore.keys;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;

public final class PersistentObjectKeyImpl<T extends AbstractPersistentObject> implements PersistentObjectKey<T> {

	private String alias;
	private long id;
	private Class<T> entityClass;

	PersistentObjectKeyImpl(Class<T> entityClass, String alias, long id) {
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
	public String toString() {
		return alias() + "#" + id();
	}
	

}
