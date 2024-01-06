package de.protubero.beanstore.entity;

public interface Companion<T extends AbstractPersistentObject> extends BeanStoreEntity<T> {

	
	T createInstance();
			
	
	default T createInstance(long id) {
		T newInstance = createInstance();
		newInstance.id(id);
		return newInstance;
	}

	String toString(T instance);



	void transferProperties(T origInstance, T newInstance);
	
	
}
