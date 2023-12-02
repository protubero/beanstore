package de.protubero.beanstore.base.entity;

import java.util.Map;

public interface Companion<T extends AbstractPersistentObject> extends BeanStoreEntity<T> {

	
	T createInstance();
			
	default T cloneInstance(T instance) {	
		T cloned = createInstance();
		
		instance.forEach((key, value) -> {
			cloned.put(key, instance.get(key));
		});
		cloned.id = instance.id;
		
		return cloned;
	}
	
	default T createInstance(long id) {
		T newInstance = createInstance();
		newInstance.id(id);
		return newInstance;
	}

	String toString(T instance);

	Map<String, Object> extractProperties(T instance);

	
	
	
}
