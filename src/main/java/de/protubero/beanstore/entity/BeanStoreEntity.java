package de.protubero.beanstore.entity;

/**
 * An instance of this class represents the meta data of an entity.
 * 
 * @param <T> The type of the entity instances
 */
public interface BeanStoreEntity<T extends AbstractPersistentObject> {

	/**
	 * The unique entity alias
	 * 
	 * @return an alias
	 */
	String alias();

	/**
	 * The associated java class, could be a JavaBean class or MapObject.class
	 * 
	 */
	Class<T> entityClass();

	boolean isMapCompanion();

	
	/**
	 * Does the entity represent a JavaBean class?
	 * 
	 * @return true, if entityClass() returns a JavaBean class, false if it returns MapObject.class
	 */
	default boolean isBean() {
		return !isMapCompanion();
	}
		
}
