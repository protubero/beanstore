package de.protubero.beanstore.plugins.search;

import de.protubero.beanstore.base.entity.InstanceKey;

public class SearchResult implements InstanceKey {

	private String id;
	private String type;
	
	public SearchResult(String id, String type) {
		this.id = id;
		this.type = type;
	}

	public String getId() {
		return id;
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
	public Long id() {
		return Long.valueOf(getId());
	}
	
	
}
