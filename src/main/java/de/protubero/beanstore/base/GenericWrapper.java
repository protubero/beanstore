package de.protubero.beanstore.base;

public class GenericWrapper<T> {

	private T wrappedObject;

	public T getWrappedObject() {
		return wrappedObject;
	}

	public void setWrappedObject(T wrappedObject) {
		this.wrappedObject = wrappedObject;
	}
	
}
