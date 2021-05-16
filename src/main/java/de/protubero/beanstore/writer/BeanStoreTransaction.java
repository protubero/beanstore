package de.protubero.beanstore.writer;

import de.protubero.beanstore.base.AbstractEntity;

public interface BeanStoreTransaction extends BaseTransaction {

	
	<T extends AbstractEntity> T create(Class<T> aClass);
	
	<T extends AbstractEntity> void delete(Class<T> aClass, long id);
	
	
	
}
