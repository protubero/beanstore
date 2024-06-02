package de.protubero.beanstore.linksandlabels;

import java.util.Objects;

import de.protubero.beanstore.entity.PersistentObjectKey;

public class LinkValue {

	private PersistentObjectKey<?> key;
	private String type;
	
	public LinkValue(PersistentObjectKey<?> key, String type) {
		this.key = key;
		this.type = type;
	}

	public PersistentObjectKey<?> getKey() {
		return key;
	}

	public String getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return ((LinkValue) obj).key.equals(key) &&
				Objects.equals(((LinkValue) obj).type, type);
	}
	
}
