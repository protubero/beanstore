package de.protubero.beanstore.plugins.search;

import java.util.Objects;

import de.protubero.beanstore.entity.InstanceKey;

public class SearchResult implements InstanceKey {

	private Long id;
	private String type;
	
	public SearchResult(String id, String type) {
		this.id = Long.parseLong(id);
		this.type = Objects.requireNonNull(type);
	}

	public String getId() {
		return id.toString();
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
		return id;
	}
	
	
}
