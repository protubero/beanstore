package de.protubero.beanstore.plugins.search;

import java.util.Objects;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.PersistentObjectKey;

public class SearchResult implements PersistentObjectKey<AbstractPersistentObject> {

	private long id;
	private String type;
	
	public SearchResult(String id, String type) {
		this.id = Long.parseLong(id);
		this.type = Objects.requireNonNull(type);
	}

	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return type + "[" + id + "]";
	}
	
	@Override
	public String alias() {
		return type;
	}

	@Override
	public long id() {
		return id;
	}

	@Override
	public Class<AbstractPersistentObject> entityClass() {
		return null;
	}
	
	
}
