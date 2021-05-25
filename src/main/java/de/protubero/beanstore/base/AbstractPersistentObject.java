package de.protubero.beanstore.base;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public abstract class AbstractPersistentObject implements Map<String, Object>, Comparable<AbstractPersistentObject>, InstanceKey {

	public static enum State {
		INSTANTIATED(false, false),
		INPLACEUPDATE(false, false),
		NEW(false, true), 		 
		DETACHED(false, true), 		 
		READY(true, false),  	
		OUTDATED(true, false); 	
		
		private boolean immutable;
		private boolean recordChanges;

		private State(boolean immutable, boolean recordChanges) {
			this.recordChanges = recordChanges;
			this.immutable = immutable;
			
			if (immutable && recordChanges) {
				throw new AssertionError();
			}
		}
	}

	public static enum Transition {
		INSTANTIATED_TO_NEW(State.INSTANTIATED, State.NEW),
		INSTANTIATED_TO_READY(State.INSTANTIATED, State.READY),
		INSTANTIATED_TO_DETACHED(State.INSTANTIATED, State.DETACHED),
		NEW_TO_READY(State.NEW, State.READY),
		READY_TO_OUTDATED(State.READY, State.OUTDATED),
		READY_TO_INPLACEUPDATE(State.READY, State.INPLACEUPDATE),
		INPLACEUPDATE_TO_READY(State.INPLACEUPDATE, State.READY);
		
		private State fromState;
		private State toState;
		
		private Transition(State fromState, State toState) {
			this.fromState = fromState;
			this.toState = toState;
		}

		public State getFromState() {
			return fromState;
		}

		public State getToState() {
			return toState;
		}
	}
	
	@JsonProperty("id")
	protected Long id;
	
	@JsonIgnore	
	protected Compagnon<?> compagnon;
	
	@JsonIgnore	
	protected State state = State.INSTANTIATED;
	
	@JsonIgnore	
	protected AbstractPersistentObject refInstance;
	
	@JsonIgnore	
	private Map<String, Object> changes;
	
	public <T extends AbstractPersistentObject> T detach() {
		verifyState(State.READY);
		
		@SuppressWarnings({ "unchecked" })
		T result = ((Compagnon<T>) compagnon).cloneInstance((T) this);
		result.applyTransition(Transition.INSTANTIATED_TO_DETACHED);
		result.refInstance(this);
		
		return result;
	}
	
	protected void verifyState(State aState) {
		if (state != aState) {
			throw new RuntimeException("invalid state, must me " + aState);
		}
	}

	public void id(long aId) {
		if (id != null) {
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
	     return ((Compagnon) compagnon).toString(this);
	}

	public boolean outdated() {
		return state == State.OUTDATED;
	}
		
	@Override
	public int compareTo(AbstractPersistentObject obj) {
		return Long.compare(id, obj.id());
	}

	void onBeforeSetValue(String fieldName, Object object) {
		if (state.immutable) {
			throw new RuntimeException("changing value on immutable instance is prohibited " + fieldName + " -> " + object);
		}
	}
	
	void onAfterValueSet(String fieldName, Object object) {
		if (state.recordChanges) {
			if (changes == null) {
				changes = new HashMap<>();
			}
			changes.put(fieldName, object);
		}
	}

	public Map<String, Object> changes() {
		return changes;
	}
	
	public AbstractPersistentObject refInstance() {
		return refInstance;
	}

	public void refInstance(AbstractPersistentObject refInstance) {
		this.refInstance = refInstance;
	}

	public State state() {
		return state;
	}

	public void applyTransition(Transition transition) {
		if (transition.fromState != state) {
			throw new RuntimeException("state does not match");
		}
		this.state = transition.getToState();
		this.changes = null;
	}
	
	public void resetChanges() {
		changes = null;
	}

	public abstract Compagnon<?> compagnon();

	public void compagnon(Compagnon<?> compagnon) {
		this.compagnon = compagnon;
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
		return compagnon;
	}
	
	@Override
	public String alias() {
		return compagnon.alias();
	}
}
