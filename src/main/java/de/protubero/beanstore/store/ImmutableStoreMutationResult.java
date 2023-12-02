package de.protubero.beanstore.store;

import java.util.Objects;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;

public class ImmutableStoreMutationResult<T extends AbstractPersistentObject> {

	private ImmutableEntityStore<T> origEntityStore;
	private ImmutableEntityStore<T> newEntityStore;
	private ImmutableEntityStoreSet storeSet;

	private T object;

	public ImmutableStoreMutationResult(ImmutableEntityStore<T> origEntityStore, ImmutableEntityStore<T> newEntityStore, T object) {
		this.origEntityStore = Objects.requireNonNull(origEntityStore);
		this.newEntityStore = Objects.requireNonNull(newEntityStore);
		this.object = Objects.requireNonNull(object);
	}
	public T getObject() {
		return object;
	}
	public boolean changed() {
		return origEntityStore != newEntityStore;
	}
	public ImmutableEntityStore<T> getOrigEntityStore() {
		return origEntityStore;
	}
	public ImmutableEntityStore<T> getNewEntityStore() {
		return newEntityStore;
	}
	public ImmutableEntityStoreSet getStoreSet() {
		return storeSet;
	}
	void setStoreSet(ImmutableEntityStoreSet storeSet) {
		this.storeSet = storeSet;
	}
	
}