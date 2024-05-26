package de.protubero.beanstore.links;

import java.util.Objects;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.tx.Transaction;

public class Link<S extends AbstractPersistentObject, T extends AbstractPersistentObject> {
	
		
	private S sourceObj;
	private T targetObj;
	private LinkObj<S, T> linkObj;
		
	public S source() {
		return sourceObj;
	}
	
	public T target() {
		return targetObj;
	}
	
	public void delete(Transaction aTransaction) {
		aTransaction.delete(PersistentObjectKey.of(linkObj), true);
	}
}
