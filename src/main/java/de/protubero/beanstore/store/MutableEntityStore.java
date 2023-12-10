package de.protubero.beanstore.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.base.entity.Companion;

public class MutableEntityStore<T extends AbstractPersistentObject> implements EntityStore<T> {

	public static final Logger log = LoggerFactory.getLogger(MutableEntityStore.class);
	
	private Map<Long, T> objectMap = new HashMap<>();

	private Companion<T> companion;
	
	private long nextInstanceId;

	private MutableEntityStoreSet storeSet;
	
	
	public MutableEntityStore(MutableEntityStoreSet storeSet, Companion<T> companion) {
		this.storeSet = storeSet;
		this.companion = companion;
	}
	

	public T get(Long id) {
		T result = objectMap.get(Objects.requireNonNull(id));
		
		if (result == null) {
			throw new InstanceNotFoundException(companion.alias(), id);
		}
		
		return result;
	}

	public T getNullable(Long id) {
		return objectMap.get(Objects.requireNonNull(id));
	}
	
	public Optional<T> getOptional(Long id) {
		return Optional.ofNullable(objectMap.get(Objects.requireNonNull(id)));
	}

	public Stream<T> objects() {
		return objectMap.values().stream();
	}	
	
	public Companion<T> companion() {
		return companion;
	}	
	
	public Collection<T> values() {
		return objectMap.values();
	}
	
	public int size() {
		return objectMap.size();
	}	
	
	@Override
	public String toString() {
		return "Mutable entity store of " + companion;
	}
	
	public T put(T modelObject) {
		if (modelObject.companion() != companion) {
			throw new AssertionError();
		}
		if (!storeSet.isAcceptNonGeneratedIds()) {
			if (nextInstanceId <= modelObject.id()) {
				throw new AssertionError();
			}
		} else {
			if (nextInstanceId <= modelObject.id()) {
				nextInstanceId = modelObject.id() + 1;
			}
		}
		
		if (modelObject.state() != State.READY) {
			throw new AssertionError("invalid state: " + modelObject.state());
		}
		Objects.requireNonNull(modelObject.id());
		
		T result = objectMap.put(modelObject.id(), modelObject);
		
		return result; 
	}
	
	public T remove(Long id) {
		return objectMap.remove(id);
	}


	@Override
	public EntityStoreSet<?> storeSet() {
		return storeSet;
	}


	@Override
	public boolean isImmutable() {
		return false;
	}


	@Override
	public long getNextInstanceId() {
		return nextInstanceId;
	}


	public Map<Long, T> getObjectMap() {
		return objectMap;
	}

	@Override
	public long getAndIncreaseInstanceId() {
		return nextInstanceId++;
	}


	@Override
	public T internalRemoveInplace(Long instanceId) {
		return remove(instanceId);
	}


	@Override
	public T internalUpdateInplace(AbstractPersistentObject apo) {
		if (apo.companion() != companion) {
			throw new AssertionError();
		}
		
		@SuppressWarnings("unchecked")
		T result = objectMap.put(apo.id(), (T) apo);
		if (result == null) {
			throw new AssertionError();
		}
		return result;
	}


	@Override
	public T internalCreateInplace(AbstractPersistentObject anInstance) {
		if (anInstance.companion() != companion) {
			throw new AssertionError();
		}
		if (nextInstanceId <= anInstance.id()) {
			throw new AssertionError();
		}
		
		@SuppressWarnings("unchecked")
		T result = objectMap.put(anInstance.id(), (T) anInstance);
		if (result != null) {
			throw new AssertionError();
		}
		return result;
	}
	
	
}
