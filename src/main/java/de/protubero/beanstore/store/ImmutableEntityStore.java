package de.protubero.beanstore.store;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;

public final class ImmutableEntityStore<T extends AbstractPersistentObject> implements EntityStore<T> {

	public static final Logger log = LoggerFactory.getLogger(ImmutableEntityStore.class);


	private HashPMap<Long, T> objectMap;

	private Companion<T> companion;
	
	private long nextInstanceId = 0L;
	
	
	public ImmutableEntityStore(Companion<T> companion) {
		this(companion, HashTreePMap.empty(), 0L);
	}
	
	public ImmutableEntityStore(Companion<T> companion, HashPMap<Long, T> objectMap, long nextInstanceId) {
		this.companion = companion;
		this.objectMap = objectMap;
		this.nextInstanceId = nextInstanceId;
	}
	
	public T get(Long id) {
		return objectMap.get(Objects.requireNonNull(id));
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
	
	public HashPMap<Long, T> objectMap() {
		return objectMap;
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
		return "Immutable entity store of " + companion;
	}
	
	@Override
	public boolean isImmutable() {
		return true;
	}

	public ImmutableEntityStore<T> cloneStore() {
		return new ImmutableEntityStore<>(companion, objectMap, nextInstanceId);
	}

	@Override
	public T internalRemoveInplace(Long instanceId) {
		T result = objectMap.get(instanceId);
		if (result != null) {
			objectMap = objectMap.minus(instanceId);
		}	
		return result;
	}


	@SuppressWarnings("unchecked")
	@Override
	public T internalUpdateInplace(AbstractPersistentObject apo) {
		if (apo.companion() != companion) {
			throw new AssertionError();
		}
		
		T result = objectMap.get(apo.id());
		if (result != null) {
			objectMap = objectMap.plus(apo.id(), (T) apo);
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
			objectMap = objectMap.plus(anInstance.id(), (T) anInstance);
		}
		return result;
	}
	

	@Override
	public long getAndIncreaseInstanceId() {
		return nextInstanceId++;
	}

	@Override
	public long getNextInstanceId() {
		return nextInstanceId;
	}

	@Override
	public boolean isEmpty() {
		return objectMap.size() == 0;
	}
	
}
