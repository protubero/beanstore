package de.protubero.beanstore.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;

public class MutableEntityStore<T extends AbstractPersistentObject> implements EntityStore<T> {

	public static final Logger log = LoggerFactory.getLogger(MutableEntityStore.class);
	
	private Map<Long, T> objectMap = new HashMap<>();

	private Companion<T> companion;
	
	private long nextInstanceId;

	private boolean acceptNonGeneratedIds;
	
	
	public MutableEntityStore(Companion<T> companion, boolean acceptNonGeneratedIds) {
		this.companion = companion;
		this.acceptNonGeneratedIds = acceptNonGeneratedIds;
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
	public boolean isEmpty() {
		return objectMap.size() == 0;
	}
	
	@Override
	public String toString() {
		return "Mutable entity store of " + companion;
	}
	
	public T put(T modelObject) {
		if (modelObject.companion() != companion) {
			throw new AssertionError();
		}
		if (!acceptNonGeneratedIds) {
			if (nextInstanceId <= modelObject.id()) {
				throw new AssertionError();
			}
		} else {
			if (nextInstanceId <= modelObject.id()) {
				nextInstanceId = modelObject.id() + 1;
			}
		}
		
		Objects.requireNonNull(modelObject.id());
		
		T result = objectMap.put(modelObject.id(), modelObject);
		
		return result; 
	}
	
	public T remove(Long id) {
		return objectMap.remove(id);
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


	@SuppressWarnings("unchecked")
	@Override
	public T internalUpdateInplace(AbstractPersistentObject apo) {
		if (apo.companion() != companion) {
			throw new AssertionError();
		}
		
		T result = objectMap.get(apo.id());
		if (result != null) {
			objectMap.put(apo.id(), (T) apo);
		}	
		return result;
	}


	@SuppressWarnings("unchecked")
	@Override
	public T internalCreateInplace(AbstractPersistentObject anInstance) {
		if (anInstance.companion() != companion) {
			throw new AssertionError();
		}
		if (nextInstanceId <= anInstance.id()) {
			throw new AssertionError();
		}
		
		T result = objectMap.get(anInstance.id());
		if (result == null) {
			objectMap.put(anInstance.id(), (T) anInstance);
		}
		return result;
	}
	
	
}
