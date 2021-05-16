package de.protubero.beanstore.store;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.Compagnon;
import de.protubero.beanstore.base.AbstractPersistentObject.State;

public class EntityStore<T extends AbstractPersistentObject> {

	public static final Logger log = LoggerFactory.getLogger(EntityStore.class);
	
	private HashPMap<Long, T> objectMap = HashTreePMap.empty();

	private Compagnon<T> compagnon;
	
	private Long nextInstanceId = null;
	
	public EntityStore(Compagnon<T> compagnon) {
		this.compagnon = compagnon;
	}
	
	public T newInstance() {
		T result = compagnon.createInstance();
		return result;
	}
	
	public T get(Long id) {
		T result = objectMap.get(Objects.requireNonNull(id));
		
		if (result == null) {
			throw new InstanceNotFoundException(compagnon.alias(), id);
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

	
	public T put(T modelObject) {
		if (modelObject.state() != State.READY) {
			throw new AssertionError("invalid state: " + modelObject.state());
		}
		T result = objectMap.get(modelObject.id());
		objectMap = objectMap.plus(modelObject.id(), modelObject);
		if (nextInstanceId == null || modelObject.id().longValue() >= nextInstanceId.longValue()) {
			nextInstanceId = modelObject.id() + 1;
		}
		return result;
	}

	public T remove(Long id) {
		T result = objectMap.get(id);
		if (result != null) {
			objectMap = objectMap.minus(id);
		}	
		return result;
	}

	public T remove(T instance) {
		return remove(instance.id());
	}

	public int size() {
		return objectMap.size();
	}

	public Compagnon<T> getCompagnon() {
		return compagnon;
	}

	public Collection<T> values() {
		return objectMap.values();
	}

	public long getNextInstanceId() {
		if (nextInstanceId == null) {
			nextInstanceId = 0l;
		}
		
		long result = nextInstanceId;
		nextInstanceId++;
		return result;
	}



}
