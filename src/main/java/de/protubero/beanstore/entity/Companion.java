package de.protubero.beanstore.entity;

import java.util.Map;

public interface Companion<T extends AbstractPersistentObject> extends BeanStoreEntity<T> {

	
	T createInstance();
			
	
	default T createInstance(long id) {
		T newInstance = createInstance();
		newInstance.id(id);
		return newInstance;
	}

	String toString(T instance);

	Map<String, Object> extractProperties(T instance);


	void transferProperties(T origInstance, T newInstance);
	
	
}
