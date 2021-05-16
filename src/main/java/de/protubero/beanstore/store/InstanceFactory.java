package de.protubero.beanstore.store;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;

public interface InstanceFactory {

	<T extends AbstractPersistentObject> T newInstance(String alias);

	<T extends AbstractEntity> T newInstance(Class<T> aClass);
	
	
}
