package de.protubero.beanstore.base.entity;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.protubero.beanstore.persistence.base.KeyValuePair;


public abstract class AbstractPersistentObject implements Map<String, Object>, Comparable<AbstractPersistentObject>, InstanceKey {

	public static enum State {
		INSTANTIATED,
		RECORD,
		RECORDED,
		PREPARE, 		 
		STORED,  	
		OUTDATED; 	
	}
	
	@JsonProperty("id")
	protected Long id;
	
	@JsonProperty("version")
	protected int version;
	
	@JsonIgnore	
	protected Companion<?> companion;
	
	@JsonIgnore	
	protected State state = State.INSTANTIATED;

	public AbstractPersistentObject() {
		if (!(this instanceof GeneratedClass)) {
			throw new RuntimeException("Entities are always instantiated by the BeanStore");
		}
	}

	protected abstract void onStateChange(State state2, State newState);

	protected abstract void recordChange(String fieldName);
	
	public abstract KeyValuePair[] changes(); 
	
	public void id(long aId) {
		if (id != null && !(state == State.PREPARE)) {
			throw new AssertionError();
		}
		id = aId;
	}

	@Override
	public Long id() {
		return id;
	}
	 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String toString() {
	    return ((Companion) companion).toString(this);
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
			break;
		default:
			throw new AssertionError();
		}
	}


	public State state() {
		return state;
	}

	public void state(State newState) {
		switch (state) {
		case INSTANTIATED:
			switch (newState) {
			case RECORD:
			case PREPARE:
				state = newState;
				onStateChange(state, newState);
				return;
			default:
				throw new AssertionError();
			}
		case PREPARE:
			switch (newState) {
			case STORED:
				state = newState;
				onStateChange(state, newState);
				return;
			default:
				throw new AssertionError();
			}
		case STORED:
			switch (newState) {
			case OUTDATED:
				state = newState;
				onStateChange(state, newState);
				return;
			default:
				throw new AssertionError();
			}
		case RECORD:
			switch (newState) {
			case RECORDED:
				state = newState;
				onStateChange(state, newState);
				return;
			default:
				throw new AssertionError();
			}
		default:
			throw new AssertionError();
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
	
	@Override
	public String alias() {
		return companion.alias();
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

}
