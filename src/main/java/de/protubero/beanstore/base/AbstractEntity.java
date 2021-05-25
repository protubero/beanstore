package de.protubero.beanstore.base;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public  class AbstractEntity extends AbstractPersistentObject {

	public static class MapEntry implements Map.Entry<String, Object> {

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
		public Object setValue(Object value) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public EntityCompagnon<?> compagnon() {
		return (EntityCompagnon<?>) super.compagnon;
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
		return compagnon().hasProperty((String) key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(Object key) {
		try {
			return PropertyUtils.getSimpleProperty(this, (String) key);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object put(String key, Object value) {
		try {
			Object result = PropertyUtils.getSimpleProperty(this, (String) key);
			PropertyUtils.setSimpleProperty(this, key, value);
			return result;
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object remove(Object key) {
		throw new UnsupportedOperationException();
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
		return compagnon().propertySet();
	}

	@Override
	public Collection<Object> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		Set<Entry<String, Object>> resultSet = new HashSet<>();
		for (PropertyDescriptor descriptor :  compagnon().getDescriptors()) {
				resultSet.add(new MapEntry(descriptor.getName(), 
						get(descriptor.getName())));
		};
		return resultSet;
	}

	
}
