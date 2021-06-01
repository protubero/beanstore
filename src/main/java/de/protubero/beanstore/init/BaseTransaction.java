package de.protubero.beanstore.init;

import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.store.BeanStoreReadAccess;

public interface BaseTransaction {

	BeanStoreReadAccess read();
	
	<T extends AbstractPersistentObject> T create(String alias);
	
	<T extends AbstractPersistentObject> T update(T instance);

	<T extends AbstractPersistentObject> void delete(String alias, long id);
	
	<T extends AbstractPersistentObject> void delete(T instance);
	
}
