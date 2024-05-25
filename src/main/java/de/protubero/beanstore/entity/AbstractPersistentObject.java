package de.protubero.beanstore.entity;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.protubero.beanstore.links.LinkPSet;
import de.protubero.beanstore.persistence.api.KeyValuePair;

@JsonSerialize(using = CustomSerializer.class)
public abstract class AbstractPersistentObject implements Map<String, Object>, Comparable<AbstractPersistentObject> {

	public static enum State {
		UNMANAGED,
		INSTANTIATED,
		RECORD,
		RECORDED,
		PREPARE, 		 
		STORED,  	
		OUTDATED; 	
	}
	
	private Long id;
	
	private int version;
	
	private Companion<?> companion;
	
	private State state;
	
	static {
		Field idField;
		try {
			idField = AbstractPersistentObject.class.getDeclaredField("id");
			idField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		} 
	}

	protected abstract void onStateChange(State state2, State newState);

	protected abstract void recordChange(String fieldName);
	
	public abstract KeyValuePair[] changes(); 
	
	public AbstractPersistentObject() {
	}
	
	public void id(long aId) {
		if (id != null || (state != State.INSTANTIATED && state != State.RECORD && state != State.RECORDED)) {
			throw new AssertionError();
		}
		id = aId;
	}

	public Long id() {
		return id;
	}

	 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String toString() {
		if (state == State.UNMANAGED) {
			return super.toString();
		} else {
			return ((Companion) companion).toString(this);
		}	
	}

	public boolean outdated() {
		return state == State.OUTDATED;
	}
		
	@Override
	public int compareTo(AbstractPersistentObject obj) {
		return Long.compare(id, obj.id());
	}

	void onBeforeSetValue(String fieldName, Object object) {
		switch (state) {
		case INSTANTIATED:
			throw new RuntimeException("changing value on instantiated instance is prohibited " + fieldName + " -> " + object);
		case STORED:
		case OUTDATED:
			throw new RuntimeException("changing value on immutable instance is prohibited " + fieldName + " -> " + object);
		case RECORDED:
			throw new RuntimeException("changing value aftre recording is prohibited " + fieldName + " -> " + object);
		default:
			// NOP
		}
	}

	
	void onBeforeChange() {
		switch (state) {
		case INSTANTIATED:
			throw new RuntimeException("changing values on instaniated instance is prohibited");
		case STORED:
		case OUTDATED:
			throw new RuntimeException("changing values on immutable instances is prohibited");
		case RECORDED:
			throw new RuntimeException("changing values on instance afetr Recording is prohibited");
		default:
			// NOP
		}
	}
	
	void onAfterValueSet(String fieldName, Object object) {
		switch (state) {
		case RECORD:
			recordChange(fieldName);
		case PREPARE:
		case UNMANAGED:
			break;
		default:
			throw new AssertionError();
		}
	}


	public State state() {
		return state;
	}

	public void state(State newState) {
		Objects.requireNonNull(newState);
		
		if (state == null) {
			state = newState;
			return;
		}
		
		switch (state) {
		case UNMANAGED:
			throw new RuntimeException("Invalid state transition from " + state + " to " + newState);
		case INSTANTIATED:
			switch (newState) {
			case RECORD:
			case PREPARE:
				state = newState;
				onStateChange(state, newState);
				return;
			default:
				throw new RuntimeException("Invalid state transition from " + state + " to " + newState);
			}
		case PREPARE:
			switch (newState) {
			case STORED:
				state = newState;
				onStateChange(state, newState);
				return;
			default:
				throw new RuntimeException("Invalid state transition from " + state + " to " + newState);
			}
		case STORED:
			switch (newState) {
			case OUTDATED:
				state = newState;
				onStateChange(state, newState);
				return;
			default:
				throw new RuntimeException("Invalid state transition from " + state + " to " + newState);
			}
		case RECORD:
			switch (newState) {
			case RECORDED:
				state = newState;
				onStateChange(state, newState);
				return;
			default:
				throw new RuntimeException("Invalid state transition from " + state + " to " + newState);
			}
		default:
			throw new RuntimeException("Invalid state transition from " + state + " to " + newState);
		}
	}

	public Companion<?> companion() {
		return companion;
	}

	
	public void companion(Companion<?> companion) {		
		if (this.companion != null) {
			throw new AssertionError();
		}
		this.companion = Objects.requireNonNull(companion);
	}

	public String getString(String key) {
		return (String) get(key);
	}

	public Integer getInteger(String key) {
		return (Integer) get(key);
	}
	
	public Long getLong(String key) {
		return (Long) get(key);
	}
	
	public BeanStoreEntity<?> entity() {
		return companion;
	}
	
	public String alias() {
		if (state == State.UNMANAGED) {
			return aliasOf(this);
		} else {
			return companion.alias();
		}	
	}
	
	public int version() {
		return version;
	}

	public void version(int version) {
		this.version = version;
	}
	
	public static String aliasOf(AbstractPersistentObject apo) {
		return aliasOf(apo.getClass());
	}
	
	public static String aliasOf(Class<? extends AbstractPersistentObject> apoClass) {
		Entity entityAnnotation = apoClass.getAnnotation(Entity.class);
		if (entityAnnotation == null) {
			throw new BeanStoreException("Missing entity annotation at class " + apoClass);
		}
		return entityAnnotation.alias();
	}
	
	private LinkPSet links = LinkPSet.empty();

	public LinkPSet getLinks() {
		return links;
	}

	public void setLinks(LinkPSet links) {
		this.links = links;
	}

}
