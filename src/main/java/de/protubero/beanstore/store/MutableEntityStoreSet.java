package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.Companion;

public class MutableEntityStoreSet implements EntityStoreSet<MutableEntityStore<?>> {

	public static final Logger log = LoggerFactory.getLogger(MutableEntityStoreSet.class);
		
	private List<MutableEntityStore<?>> storeList = new ArrayList<>();
	private boolean acceptNonGeneratedIds = false;

	public MutableEntityStoreSet(Iterable<Companion<?>> companionList) {
		for (Companion<?> companion : companionList) {
			MutableEntityStore<?> store = new MutableEntityStore<>(this, companion);
			storeList.add(store);
		}
	}

	public MutableEntityStoreSet(CompanionShip companionSet) {
		companionSet.companions().forEach(companion -> {
			MutableEntityStore<?> store = new MutableEntityStore<>(this, companion);
			storeList.add(store);
		});
	}
	
	public MutableEntityStoreSet() {
	}

	@Override
	public Iterator<MutableEntityStore<?>> iterator() {
		return storeList.iterator();
	}


	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractPersistentObject> EntityStore<T> store(String alias) {
		for (MutableEntityStore<?> store : storeList) {
			if (store.companion().alias().equals(alias)) {
				return (EntityStore<T>) store;
			}
		}
		throw new RuntimeException("unknown store: " + alias);
	}

	@SuppressWarnings("unchecked")
	@Override	
	public <T extends AbstractPersistentObject> EntityStore<T> store(Class<T> aClass) {
		for (MutableEntityStore<?> store : storeList) {
			if (store.companion().entityClass() == aClass) {
				return (EntityStore<T>) store;
			}
		}
		throw new RuntimeException("unknown entity class: " + aClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractPersistentObject> Optional<EntityStore<T>> storeOptional(String alias) {
		for (MutableEntityStore<?> store : storeList) {
			if (store.companion().alias().equals(alias)) {
				return Optional.of((EntityStore<T>) store);
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractPersistentObject> Optional<EntityStore<T>> storeOptional(Class<T> aClass) {
		for (MutableEntityStore<?> store : storeList) {
			if (store.companion().entityClass() == aClass) {
				return Optional.of((EntityStore<T>) store);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean empty() {
		for (MutableEntityStore<?> store : storeList) {
			if (store.size() > 0) {
				return false;
			}
		}
		return true;
	}


	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> T remove(String alias, Long id) {
		return ((MutableEntityStore<T>) store(alias)).remove(id);
	}

	public <T extends AbstractPersistentObject> T remove(Class<T> aClass, Long id) {
		return ((MutableEntityStore<T>) store(aClass)).remove(id);
	}

	public <T extends AbstractPersistentObject> T remove(EntityStore<T> store, Long id) {
		if (store.storeSet() != this) {
			throw new AssertionError("Store does not belong to this store set");
		}
		return ((MutableEntityStore<T>) store).remove(id);
	}

	public <T extends AbstractPersistentObject> T put(T modelObject) {
		return put((MutableEntityStore<T>) store(modelObject), modelObject);
	}
	
	public <T extends AbstractPersistentObject> T put(EntityStore<T> store, T modelObject) {
		if (store.storeSet() != this) {
			throw new AssertionError("Store does not belong to this store set");
		}
		return ((MutableEntityStore<T>) store).put(modelObject);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> EntityStore<T> register(Companion<?> companion) {
		MutableEntityStore<?> store = new MutableEntityStore<>(this, companion);
		storeList.add(store);
		return (EntityStore<T>) store;
	}

	@Override
	public boolean isImmutable() {
		return false;
	}

	@Override
	public EntityStoreSet<MutableEntityStore<?>> internalCloneStoreSet() {
		return this;
	}

	@Override
	public Stream<Companion<?>> companions() {
		return storeList.stream().map(s -> s.companion());
	}

	public boolean isAcceptNonGeneratedIds() {
		return acceptNonGeneratedIds;
	}

	public void setAcceptNonGeneratedIds(boolean acceptNonGeneratedIds) {
		this.acceptNonGeneratedIds = acceptNonGeneratedIds;
	}


	
	
}
