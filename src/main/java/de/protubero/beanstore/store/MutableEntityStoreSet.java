package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;

public class MutableEntityStoreSet implements EntityStoreSet<MutableEntityStore<?>> {

	public static final Logger log = LoggerFactory.getLogger(MutableEntityStoreSet.class);
		
	private List<MutableEntityStore<?>> storeList = new ArrayList<>();
	private boolean acceptNonGeneratedIds = false;
	private int version = 0;
	

	public MutableEntityStoreSet(List<MutableEntityStore<?>> aStoreList, boolean acceptNonGeneratedIds, int version) {
		this.storeList = Objects.requireNonNull(aStoreList);
		this.acceptNonGeneratedIds = acceptNonGeneratedIds;
		this.version = version;
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
	public boolean hasNoData() {
		for (MutableEntityStore<?> store : storeList) {
			if (store.size() > 0) {
				return false;
			}
		}
		return true;
	}


	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> EntityStore<T> register(Companion<?> companion) {
		MutableEntityStore<?> store = new MutableEntityStore<>(companion, acceptNonGeneratedIds);
		storeList.add(store);
		return (EntityStore<T>) store;
	}

	@Override
	public boolean isImmutable() {
		return false;
	}

	@Override
	public EntityStoreSet<MutableEntityStore<?>> internalCloneStoreSet() {
		return new MutableEntityStoreSet(storeList, acceptNonGeneratedIds, version + 1);
	}

	public boolean isAcceptNonGeneratedIds() {
		return acceptNonGeneratedIds;
	}

	public void setAcceptNonGeneratedIds(boolean acceptNonGeneratedIds) {
		this.acceptNonGeneratedIds = acceptNonGeneratedIds;
	}


	@Override
	public boolean hasNoEntityStores() {
		return storeList.isEmpty();
	}

	@Override
	public int version() {
		return version;
	}


	public void version(int aVersion) {
		this.version = aVersion;
	}
	
	
}
