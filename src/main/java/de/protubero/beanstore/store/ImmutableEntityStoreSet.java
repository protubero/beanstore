package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.pcollections.HashTreePMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;

public class ImmutableEntityStoreSet extends AbstractEntityStoreSet<ImmutableEntityStore<?>> {

	public static final Logger log = LoggerFactory.getLogger(ImmutableEntityStoreSet.class);
	
	
	private ImmutableEntityStore<?>[] storeList;
	private int version;


	private ImmutableEntityStoreSet(ImmutableEntityStore<?>[] aStoreList, int version) {
		storeList = Objects.requireNonNull(aStoreList);
		this.version = version;
	}
	
	public ImmutableEntityStoreSet(Iterable<Companion<?>> companionList, int version) {
		List<ImmutableEntityStore<?>> tempStoreList = new ArrayList<>();
		for (Companion<?> companion : companionList) {
			ImmutableEntityStore<?> store = new ImmutableEntityStore<>(companion);
			tempStoreList.add(store);
		}
		storeList = tempStoreList.toArray(new ImmutableEntityStore<?>[tempStoreList.size()]);
		
		this.version = version;
	}
	
	
	@SuppressWarnings("unchecked")
	public ImmutableEntityStoreSet(ImmutableEntityStoreBase<?>[] entityStoreBaseList, int version) {
		storeList = new ImmutableEntityStore<?>[entityStoreBaseList.length];
		for (int idx = 0; idx < entityStoreBaseList.length; idx++) {
			ImmutableEntityStoreBase<?> storeBase = entityStoreBaseList[idx];
			storeList[idx] = 
					new ImmutableEntityStore<AbstractPersistentObject>(
							(Companion<AbstractPersistentObject>) storeBase.getCompanion(), 
							HashTreePMap.from((Map<Long, AbstractPersistentObject>)storeBase.getObjectMap()),
							storeBase.getNextInstanceId());
		}
		
		this.version = version;
	}

	
	@Override
	public Iterator<ImmutableEntityStore<?>> iterator() {
		
		return new Iterator<ImmutableEntityStore<?>>() {
			int idx = -1;

			@Override
			public boolean hasNext() {
				return idx != storeList.length - 1;
			}

			@Override
			public ImmutableEntityStore<?> next() {
				if (idx == storeList.length - 1) {
					return null;
				} else  {
					idx++;
					return storeList[idx];
				}
			}
			
		};
	}

	@SuppressWarnings("unchecked")
	public ImmutableEntityStore<?> store(String alias) {
		Objects.requireNonNull(alias);
		for (ImmutableEntityStore<?> store : storeList) {
			if (store.companion().alias().equals(alias)) {
				return store;
			}
		}
		throw new RuntimeException("Invalid store alias: " + alias);
	}
	
	@SuppressWarnings("unchecked")
	public Optional<ImmutableEntityStore<?>> storeOptional(String alias) {
		Objects.requireNonNull(alias);
		for (ImmutableEntityStore<?> store : storeList) {
			if (store.companion().alias().equals(alias)) {
				return Optional.ofNullable(store);
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> ImmutableEntityStore<T> store(Class<T> entityClass) {
		Objects.requireNonNull(entityClass);
		for (ImmutableEntityStore<?> store : storeList) {
			if (store.companion().isBean() && store.companion().entityClass().equals(entityClass)) {
				return (ImmutableEntityStore<T>) store;
			}
		}
		throw new RuntimeException("Invalid store entity class: " + entityClass);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> Optional<EntityStore<T>> storeOptional(Class<T> entityClass) {
		Objects.requireNonNull(entityClass);
		for (ImmutableEntityStore<?> store : storeList) {
			if (store.companion().isBean() &&  store.companion().entityClass().equals(entityClass)) {
				return Optional.ofNullable((ImmutableEntityStore<T>) store);
			}
		}
		return Optional.empty();
	}
		
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> ImmutableEntityStore<T> store(T ref) {
		return (ImmutableEntityStore<T>) store(AbstractPersistentObject.aliasOf(ref));
	}	

	@Override
	public boolean hasNoData() {
		for (ImmutableEntityStore<?> store : storeList) {
			if (store.size() > 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean hasNoEntityStores() {
		return storeList.length == 0;
	}
	
	@Override
	public boolean isImmutable() {
		return true;
	}

	@Override
	public ImmutableEntityStoreSet internalCloneStoreSet() {
		ImmutableEntityStore<?>[] newStoreList = new ImmutableEntityStore<?>[storeList.length];
		for (int idx = 0; idx < storeList.length; idx++) {
			newStoreList[idx] = storeList[idx].cloneStore();
		}
		return new ImmutableEntityStoreSet(newStoreList, version + 1);
	}

	@Override
	public int version() {
		return version;
	}



	
}
