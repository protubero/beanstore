package de.protubero.beanstore.base.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.protubero.beanstore.persistence.base.KeyValuePair;
import de.protubero.beanstore.persistence.base.PersistentProperty;

public final class MapObject extends AbstractPersistentObject {

	private Map<String, Object> properties;
	
	@JsonIgnore
	private PersistentProperty[] recordedValues;
	
	@Override
	public int size() {
		return properties.size();
	}
	
	@Override
	public boolean isEmpty() {
		return properties.isEmpty();
	}
	
	@Override
	public boolean containsKey(Object key) {
		return properties.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return properties.containsValue(value);
	}
	
	@Override
	public Object get(Object key) {
		return properties.get(key);
	}
	
	@Override
	public Object put(String key, Object value) {
		onBeforeSetValue(key, value);
		
		Object result = properties.put(key, value);
		
		onAfterValueSet(key, value);
		return result;
	}
	
	@Override
	public Object remove(Object key) {
		// setting null and removing a value have same semantics in state PREPARE
		onBeforeSetValue((String) key, null);
		Object result = properties.remove(key);
		onAfterValueSet((String) key, null);
		return result;
	}
	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		onBeforeChange();
		properties.forEach((key, value) -> {
			 put(key, value);
		});
	}
	@Override
	public void clear() {
		onBeforeChange();
		properties.clear();
	}
	@Override
	public Set<String> keySet() {
		return properties.keySet();
	}
	@Override
	public Collection<Object> values() {
		return properties.values();
	}
	@Override
	public Set<Entry<String, Object>> entrySet() {
		return properties.entrySet();
	}
	
	@Override
	protected void onStateChange(State fromState, State toState) {
		switch (toState) {
		case RECORDED:
			recordedValues = new PersistentProperty[properties.size()];
			int idx = 0;
			for (var entry : properties.entrySet()) {
				recordedValues[idx++]= PersistentProperty.of(entry.getKey(), entry.getValue());
			}
			properties = null;
			break;
		case RECORD:
		case PREPARE:
			properties = new HashMap<>();
			break;
		case STORED:
			properties = Collections.unmodifiableMap(properties);
			break;
		default:
			// NOP
		}
	}
	
	@Override
	public MapObjectCompanion companion() {
		return (MapObjectCompanion) super.companion();
	}
	

	@Override
	protected void recordChange(String fieldName) {
		// ignore
	}

	@Override
	public KeyValuePair[] changes() {
		return recordedValues;
	}



}
