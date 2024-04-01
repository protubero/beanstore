package de.protubero.beanstore.builder;

import de.protubero.beanstore.api.BeanStoreSnapshot;
import de.protubero.beanstore.entity.MapObject;

/**
 * The kind of transaction which is used with migrations. <br>
 * Migration only operate on MapObjects. 
 *
 */
public interface MigrationTransaction {

	/**
	 * Create a new instance of an entity with alias <i>alias</i>.<br>
	 */
	MapObject create(String alias);
	
	
	/**
	 * Update an existing instance. To update any properties, 
	 * just set them on the instance returned by this method. 
	 */
	MapObject update(MapObject instance);

	MapObject update(String alias, long id);
	
	/**
	 * Delete the instance with the given coordinates. If it does not exist (or has already been deleted), 
	 * the transaction fails. 
	 */
	void delete(String alias, long id);
	
	/**
	 * Delete the given instance. If it has already been deleted, the transaction fails.
	 */
	void delete(MapObject instance);
	
	
	BeanStoreSnapshot snapshot();
	
}
