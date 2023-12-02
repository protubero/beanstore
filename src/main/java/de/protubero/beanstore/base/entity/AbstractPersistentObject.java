package de.protubero.beanstore.base.entity;

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

		public boolean isImmutable() {
			return immutable;
		}

		public boolean isRecordChanges() {
			return recordChanges;
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
	protected Companion<?> companion;
	
	@JsonIgnore	
	protected State state = State.INSTANTIATED;
	
	@JsonIgnore	
	protected AbstractPersistentObject refInstance;
	
	@JsonIgnore	
	private Map<String, Object> changes;
	
	public <T extends AbstractPersistentObject> T detach() {
		verifyState(State.READY);
		
		@SuppressWarnings({ "unchecked" })
		T result = ((Companion<T>) companion).cloneInstance((T) this);
		result.applyTransition(Transition.INSTANTIATED_TO_DETACHED);
		result.refInstance(this);
		
		return result;
	}

	protected void checkIfCreatedByStore() {
		if (companion == null) {
			throw new BeanStoreException("Java Bean Instance has not been created by a Bean Store");
		}
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
		if (companion == null) {
			return null;
		}
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
		if (state.immutable) {
			throw new RuntimeException("changing value on immutable instance is prohibited " + fieldName + " -> " + object);
		}
	}

	void onBeforeChange() {
		if (state.immutable) {
			throw new RuntimeException("changing value on immutable instance is prohibited");
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

	public abstract Companion<?> companion();

	public void companion(Companion<?> companion) {
		this.companion = companion;
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
		if (companion == null) {
			return AbstractPersistentObject.aliasOf(this);
		} else {
			return companion.alias();
		}	
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
