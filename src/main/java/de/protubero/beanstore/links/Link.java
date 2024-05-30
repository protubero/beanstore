package de.protubero.beanstore.links;

import java.util.Objects;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.tx.Transaction;

public final class Link<S extends AbstractPersistentObject, T extends AbstractPersistentObject> {
	
		
	private S sourceObj;
	private T targetObj;
	private LinkObj<S, T> linkObj;

	public Link(S sourceObj, T targetObj, LinkObj<S, T> linkObj) {
		this.sourceObj = Objects.requireNonNull(sourceObj);
		this.targetObj = Objects.requireNonNull(targetObj);
		this.linkObj = Objects.requireNonNull(linkObj);
	}
	
	public S source() {
		return sourceObj;
	}
	
	public T target() {
		return targetObj;
	}
	
	public String type() {
		return linkObj.getType();
	}
	
	public void delete(Transaction aTransaction) {
		aTransaction.delete(PersistentObjectKey.of(linkObj), true);
	}
	
	@Override
	public int hashCode() {
		return linkObj.id().intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		return ((LinkObj<?, ?>) obj).id().equals(linkObj.id());
	}

	public LinkObj<S, T> getLinkObj() {
		return linkObj;
	}
	
}
