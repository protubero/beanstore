package de.protubero.beanstore.links;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.PersistentObjectKey;

public class Link<S extends AbstractPersistentObject, T extends AbstractPersistentObject> {

	private PersistentObjectKey<S> source;
	private PersistentObjectKey<T> target;
	private String type;
	
	private volatile S sourceObj;
	private volatile T targetObj;
	
}
