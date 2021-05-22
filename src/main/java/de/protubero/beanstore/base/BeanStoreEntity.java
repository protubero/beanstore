package de.protubero.beanstore.base;

/**
 * An instance of this class represents the meta data of an entity.
 * 
 * @param <T>
 */
public interface BeanStoreEntity<T extends AbstractPersistentObject> {

	/**
	 * The entity alias
	 * 
	 * @return an alias
	 */
	String alias();

	Class<T> entityClass();
	
	default boolean isBean() {
		return AbstractEntity.class.isAssignableFrom(entityClass());
	}
	
	
}
