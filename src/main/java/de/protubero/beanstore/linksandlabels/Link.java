package de.protubero.beanstore.linksandlabels;

import java.util.Objects;

import de.protubero.beanstore.entity.AbstractPersistentObject;

public final class Link<S extends AbstractPersistentObject, T extends AbstractPersistentObject> {
		
	private S sourceObj;
	private T targetObj;
	private String type;

	public Link(S sourceObj, T targetObj, String aType) {
		this.sourceObj = Objects.requireNonNull(sourceObj);
		this.targetObj = Objects.requireNonNull(targetObj);
		this.type = aType;
	}
	
	public S source() {
		return sourceObj;
	}
	
	public T target() {
		return targetObj;
	}
	
	public String type() {
		return type;
	}
		
	@Override
	public int hashCode() {
		return sourceObj.id().intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		return ((Link<?, ?>) obj).sourceObj == sourceObj && 
				((Link<?, ?>) obj).targetObj == targetObj &&
				Objects.equals(((Link<?, ?>) obj).type, type) ;
	}
	
}
