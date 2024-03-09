package de.protubero.beanstore.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.protubero.beanstore.persistence.api.KeyValuePair;
import de.protubero.beanstore.persistence.api.PersistentProperty;

@JsonDeserialize(using = CustomDeserializer.class)
public abstract class AbstractEntity extends AbstractPersistentObject {

	private PersistentProperty[] changes;
	
	private Set<String> changedFields;

	
	public AbstractEntity() {
		if (this instanceof GeneratedClass) {
			state(State.INSTANTIATED);			
		} else {
			state(State.UNMANAGED);	
			companion(CompanionRegistry.getOrCreateEntityCompanion(getClass()));
		}
	}
	
	
	public EntityCompanion<?> companion() {
		return (EntityCompanion<?>) super.companion();
	}

	
	@Override
	public int size() {
		return companion().propertyCount();
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

	public Object set(String key, Object value) {
		return put(key, value);
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
		return companion().entrySetOf(this);
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
			break;
		case RECORDED:
			changes = new PersistentProperty[changedFields.size()];
			int idx = 0;
			for (String fieldName : changedFields) {
				changes[idx++] = PersistentProperty.of(fieldName, get(fieldName)); 
			}
			
			break;
		default:
			// NOP
		}
	}
	
	@Override
	protected void recordChange(String fieldName) {
		changedFields.add(fieldName);
	}


	@Override
	public KeyValuePair[] changes() {
		return changes;
	}

	public AbstractEntity unmanagedCopy() {
		if (state() != State.STORED && state() != State.OUTDATED) {
			throw new RuntimeException("Invalid state to get unmanaged copy from: " + state());
		}
		
		try {
			return companion().entityClass().getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Error creating unmanaged copy", e);
		}
	}
}
