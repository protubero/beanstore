package de.protubero.beanstore.links;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Entity;
import de.protubero.beanstore.entity.PersistentObjectKey;

@Entity(alias="link")
public class LinkObj<S extends AbstractPersistentObject, T extends AbstractPersistentObject> extends AbstractEntity {

	private PersistentObjectKey<S> sourceKey;
	private PersistentObjectKey<T> targetKey;
	private String type;
		
	
	public PersistentObjectKey<S> getSourceKey() {
		return sourceKey;
	}
	
	public void setSourceKey(PersistentObjectKey<S> sourceKey) {
		this.sourceKey = sourceKey;
	}
	
	public PersistentObjectKey<T> getTargetKey() {
		return targetKey;
	}
	
	public void setTargetKey(PersistentObjectKey<T> targetKey) {
		this.targetKey = targetKey;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
		
}
