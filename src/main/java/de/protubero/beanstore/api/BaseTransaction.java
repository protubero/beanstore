package de.protubero.beanstore.api;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;

public interface BaseTransaction {

	BeanStoreReadAccess read();
	
	<T extends AbstractPersistentObject> T create(String alias);
	
	<T extends AbstractPersistentObject> T update(T instance);

	<T extends AbstractPersistentObject> void delete(String alias, long id);
	
	<T extends AbstractPersistentObject> void delete(T instance);
	
}
