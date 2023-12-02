package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

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

	public <T extends AbstractPersistentObject> ImmutableStoreMutationResult<T> put(T modelObject) {
		return put(store(modelObject), modelObject);
	}

	public <T extends AbstractPersistentObject> ImmutableStoreMutationResult<T> put(ImmutableEntityStore<T> store, T modelObject) {
		if (store.storeSet() != this) {
			throw new AssertionError("Store does not belong to this store set");
		}
		ImmutableStoreMutationResult<T> result = ((ImmutableEntityStore<T>) store).put(modelObject);
		return completeMutationResult(store, result);
	}

	private <T extends AbstractPersistentObject> ImmutableStoreMutationResult<T> completeMutationResult(
			ImmutableEntityStore<T> store, ImmutableStoreMutationResult<T> result) {
		if (result.changed()) {
			ImmutableEntityStore<?>[] newStoreList = new ImmutableEntityStore<?>[storeList.length];
			System.arraycopy(storeList, 0, newStoreList, 0, storeList.length);
			newStoreList[store.getStoreSetIndex()] = result.getNewEntityStore();
			ImmutableEntityStoreSet newStoreSet = new ImmutableEntityStoreSet(newStoreList);
			result.setStoreSet(newStoreSet);
		} else {
			result.setStoreSet(this);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> ImmutableStoreMutationResult<T> remove(String alias, Long id) {
		return (ImmutableStoreMutationResult<T>) remove(store(alias), id);
	}

	public <T extends AbstractPersistentObject> ImmutableStoreMutationResult<T> remove(Class<T> aClass, Long id) {
		return (ImmutableStoreMutationResult<T>) remove(store(aClass), id);
	}

	public <T extends AbstractPersistentObject> ImmutableStoreMutationResult<T> remove(ImmutableEntityStore<T> store, Long id) {
		if (store.storeSet() != this) {
			throw new AssertionError("Store does not belong to this store set");
		}
		ImmutableStoreMutationResult<T> result = ((ImmutableEntityStore<T>) store).remove(id);
		return completeMutationResult(store, result);
	}

	@Override
	public boolean isImmutable() {
		return true;
	}

	public ImmutableEntityStoreSet internalCloneStoreSet() {
		ImmutableEntityStore<?>[] newStoreList = new ImmutableEntityStore<?>[storeList.length];
		for (int idx = 0; idx < storeList.length; idx++) {
			newStoreList[idx] = storeList[idx].cloneStore();
		}
		return new ImmutableEntityStoreSet(newStoreList);
	}

	
}
