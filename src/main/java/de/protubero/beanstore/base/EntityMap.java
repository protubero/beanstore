package de.protubero.beanstore.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class EntityMap extends AbstractPersistentObject {

	private HashMap<String, Object> properties = new HashMap<>();
	
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
		// setting null and removing a value have same semantics
		onBeforeSetValue((String) key, null);
		Object result = properties.remove(key);
		onAfterValueSet((String) key, null);
		return result;
	}
	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		properties.forEach((key, value) -> {
			 put(key, value);
		});
	}
	@Override
	public void clear() {
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

	public EntityMapCompagnon compagnon() {
		return (EntityMapCompagnon) super.compagnon;
	}

}
