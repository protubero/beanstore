package de.protubero.beanstore.base.entity;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;


@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class AbstractEntity extends AbstractPersistentObject {

	public class MapEntry implements Map.Entry<String, Object> {

		private String key;
		private Object value;
		
		public MapEntry(String key, Object value) {
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
			Object result = AbstractEntity.this.put(key, aValue);
			this.value = aValue;
			return result;
		}
		
		@Override
		public String toString() {
			return key + ":" + value;
		}
		
	}
	
	@JsonIgnore	
	private Set<String> changedFields;

	
	public EntityCompanion<?> companion() {
		return (EntityCompanion<?>) super.companion;
	}

	
	/**
	 * Rather inperformant implementation. Not intended for everyday use.
	 */
	@Override
	public int size() {
		return (int) entrySet().stream().filter(e -> e.getValue() != null).count();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return companion().hasProperty((String) key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(Object key) {
		return companion().getProperty(this, key);
	}

	@Override
	public Object put(String key, Object value) {
		Object result = companion().getProperty(this, key);
		companion().setProperty(this, key, value);
		return result;
	}


	@Override
	public void putAll(Map<? extends String, ? extends Object> map) {
		map.forEach((key, value) -> {
			put(key, value);
		});
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		return companion().propertySet();
	}

	@Override
	public Collection<Object> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		Set<Entry<String, Object>> resultSet = new HashSet<>();
		for (PropertyDescriptor descriptor : companion().getDescriptors()) {
				resultSet.add(new MapEntry(descriptor.getName(), 
						get(descriptor.getName())));
		};
		return resultSet;
	}

	@Override
	public Map<String, Object> recordedValues()  {
		if (state != State.RECORD) {
			throw new AssertionError();
		}
		Map<String, Object> map = new HashMap<>();
		
		changedFields.forEach(fieldName -> {
			map.put(fieldName, get(fieldName));
		});
		return map;
	}


	@Override
	public Object remove(Object key) {
		if (key == null || !(key instanceof String)) {
			throw new RuntimeException("Null or non-string key");
		}
		return put((String) key, null);
	}


	@Override
	protected void onStateChange(State fromState, State newState) {
		switch (newState) {
		case RECORD:
			changedFields = new HashSet<>();
		default:
			// NOP
		}
	}
	
	@Override
	protected void recordChange(String fieldName) {
		changedFields.add(fieldName);
	}


	
}
