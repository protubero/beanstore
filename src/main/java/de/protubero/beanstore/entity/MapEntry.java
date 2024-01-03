package de.protubero.beanstore.entity;

import java.util.Map;

public class MapEntry implements Map.Entry<String, Object> {

	/**
	 * 
	 */
	private final AbstractEntity entity;
	private String key;
	private Object value;
	
	public MapEntry(AbstractEntity aEntity, String key, Object value) {
		entity = aEntity;
		this.key = key;
		this.value = value;
	}
	
	@Override
	public String getKey() {
		return key;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public Object setValue(Object aValue) {
		Object result = entity.put(key, aValue);
		this.value = aValue;
		return result;
	}
	
	@Override
	public String toString() {
		return key + ":" + value;
	}
	
}