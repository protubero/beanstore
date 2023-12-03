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

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.Companion;

public class ImmutableEntityStoreSet implements EntityStoreSet<ImmutableEntityStore<?>> {

	public static final Logger log = LoggerFactory.getLogger(ImmutableEntityStoreSet.class);
	
	
	private ImmutableEntityStore<?>[] storeList;

	private ImmutableEntityStoreSet(ImmutableEntityStore<?>[] storeList) {
		this.storeList = Objects.requireNonNull(storeList);
	}
	
	ImmutableEntityStoreSet(List<Companion<?>> companionList) {
		List<ImmutableEntityStore<?>> tempStoreList = new ArrayList<>();
		int idx = 0;
		for (Companion<?> companion : companionList) {
			ImmutableEntityStore<?> store = new ImmutableEntityStore<>(this, idx++, companion);
			tempStoreList.add(store);
		}
		storeList = tempStoreList.toArray(new ImmutableEntityStore<?>[tempStoreList.size()]);
	}
	
	
	@SuppressWarnings("unchecked")
	public ImmutableEntityStoreSet(ImmutableEntityStoreBase<?>[] entityStoreBaseList) {
		storeList = new ImmutableEntityStore<?>[entityStoreBaseList.length];
		for (int idx = 0; idx < entityStoreBaseList.length; idx++) {
			ImmutableEntityStoreBase<?> storeBase = entityStoreBaseList[idx];
			storeList[idx] = 
					new ImmutableEntityStore<AbstractPersistentObject>(
							this, 
							idx, 
							(Companion<AbstractPersistentObject>) storeBase.getCompanion(), 
							HashTreePMap.from((Map<Long, AbstractPersistentObject>)storeBase.getObjectMap()),
							storeBase.getNextInstanceId());
		}
	}

	public static ImmutableEntityStoreSet of(List<ImmutableEntityStore<?>> aStoreList) {
		return new  ImmutableEntityStoreSet( aStoreList.toArray(new ImmutableEntityStore<?>[aStoreList.size()]));
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
			if (store.companion().entityClass().equals(entityClass)) {
				return (ImmutableEntityStore<T>) store;
			}
		}
		throw new RuntimeException("Invalid store entity class: " + entityClass);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> Optional<EntityStore<T>> storeOptional(Class<T> entityClass) {
		Objects.requireNonNull(entityClass);
		for (ImmutableEntityStore<?> store : storeList) {
			if (store.companion().entityClass().equals(entityClass)) {
				return Optional.ofNullable((ImmutableEntityStore<T>) store);
			}
		}
		return Optional.empty();
	}
		
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> ImmutableEntityStore<T> store(T ref) {
		return (ImmutableEntityStore<T>) store(AbstractPersistentObject.aliasOf(ref));
	}	
	
	public boolean isEmpty() {
		for (ImmutableEntityStore<?> store : storeList) {
			if (store.size() > 0) {
				return false;
			}
		}
		return true;
	}

	<T extends AbstractPersistentObject> ImmutableEntityStoreSet exchangeEntityStore(ImmutableEntityStore<T> immutableEntityStore,
			ImmutableEntityStore<T> newEntityStore) {
		ImmutableEntityStore<?>[] newStoreList = new ImmutableEntityStore[storeList.length];
		System.arraycopy(storeList, 0, newStoreList, 0, storeList.length);
		
		for (int i = 0; i < newStoreList.length; i++) {
			ImmutableEntityStore<?> store = newStoreList[i];
			if (store == immutableEntityStore) {
				newStoreList[i] = newEntityStore;
				return new ImmutableEntityStoreSet(newStoreList);
			}
		}
		throw new AssertionError();
	}


	@Override
	public boolean empty() {
		for (ImmutableEntityStore<?> store : storeList) {
			if (store.size() > 0) {
				return false;
			}
		}
		return true;
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
		return new ImmutableEntityStoreSet(newStoreList);
	}

	
}