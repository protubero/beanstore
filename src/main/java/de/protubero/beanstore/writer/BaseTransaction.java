package de.protubero.beanstore.writer;

import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.store.BeanStoreReader;

public interface BaseTransaction {

	BeanStoreReader dataStore();
	
	<T extends AbstractPersistentObject> T create(String alias);
	
	<T extends AbstractPersistentObject> T update(T instance);

	<T extends AbstractPersistentObject> void delete(String alias, long id);
	
	<T extends AbstractPersistentObject> void delete(T instance);
	
}
