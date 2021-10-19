package de.protubero.beanstore.base.entity;

import java.util.function.Function;

public class InstanceProxy<T extends AbstractPersistentObject> implements InstanceKey {

	private T referencedObject;
	private Function<InstanceKey, T> provider;
	
	@Override
	public String alias() {
		return null;
	}

	@Override
	public Long id() {
		return null;
	}

	public T getReferencedObject() {
		return referencedObject;
	}

	public void setReferencedObject(T referencedObject) {
		this.referencedObject = referencedObject;
	}

	public Function<InstanceKey, T> getProvider() {
		return provider;
	}

	public void setProvider(Function<InstanceKey, T> provider) {
		this.provider = provider;
	}

}
