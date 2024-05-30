package de.protubero.beanstore.api;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.entity.PersistentObjectVersionKey;
import de.protubero.beanstore.links.Link;
import de.protubero.beanstore.links.LinkObj;


/**
 * Adds the explicit handling of bean-based instances to the basic transaction.
 *
 */
public interface BeanStoreTransaction  {
	
	/**
	 * Create a new instance of an entity with alias <i>alias</i>.<br>
	 */
	AbstractPersistentObject create(String alias);

	/**
	 * Create a BeanStore bean instance of the type determined by the parameter. 
	 */
	<T extends AbstractEntity> T create(Class<T> aClass);
	
	<T extends AbstractPersistentObject> T create(T templateInstance);

	void delete(PersistentObjectKey<?> key);

	void delete(PersistentObjectVersionKey<?> key);	
	
	<T extends AbstractPersistentObject> T update(PersistentObjectKey<T> key);
		
	<T extends AbstractPersistentObject> T update(PersistentObjectVersionKey<T> key);
		
	void describe(String text);
	
	<S extends AbstractPersistentObject, T extends AbstractPersistentObject> void delete(Link<S, T> link);
	
	<S extends AbstractPersistentObject, T extends AbstractPersistentObject> void link(
			PersistentObjectKey<S> sourceKey, 
			PersistentObjectKey<T> targetKey,
			String type);
	
	<S extends AbstractPersistentObject, T extends AbstractPersistentObject> void link (
			S sourceObj, 
			T targetObj,
			String type);

	void delete(PersistentObjectKey<?> key, boolean ignoreNonExistence);
	
	
}
