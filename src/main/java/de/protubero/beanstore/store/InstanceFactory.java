package de.protubero.beanstore.store;

import java.util.Map;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;

public interface InstanceFactory {

	<T extends AbstractPersistentObject> T newInstance(String alias);

	<T extends AbstractEntity> T newInstance(Class<T> aClass);

	Map<String, Object> extractProperties(AbstractPersistentObject apo);
	
}
