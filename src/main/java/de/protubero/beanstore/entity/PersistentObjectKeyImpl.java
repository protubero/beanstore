package de.protubero.beanstore.entity;

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
	
	@Override
	public int hashCode() {
		return (int) id();
	}
	
	@Override
	public boolean equals(Object obj) {
		return ((PersistentObjectKey<?>) obj).id() == id()
				&& ((PersistentObjectKey<?>) obj).alias().equals(alias());
	}

	@Override
	public boolean test(T t) {
		return t.id().longValue() == id && t.alias().equals(alias);
	}
	
}
