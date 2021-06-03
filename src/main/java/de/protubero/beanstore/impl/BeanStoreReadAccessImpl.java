package de.protubero.beanstore.impl;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.api.BeanStoreMetaInfo;
import de.protubero.beanstore.api.BeanStoreReadAccess;
import de.protubero.beanstore.api.EntityReadAccess;
import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.BeanStoreEntity;
import de.protubero.beanstore.store.EntityStore;
import de.protubero.beanstore.store.Store;

public class BeanStoreReadAccessImpl implements BeanStoreReadAccess {

	private Store store;

	BeanStoreReadAccessImpl(Store store) {
		this.store = Objects.requireNonNull(store);
	}

	@Override
	public BeanStoreReadAccess snapshot() {
		return new BeanStoreReadAccessImpl(store.snapshot());
	}

	@Override
	public <T extends AbstractEntity> Optional<EntityReadAccess<T>> entityOptional(Class<T> aClass) {
		return store.storeOptional(aClass).map(e -> new EntityReadAccessImpl<>(e));
	}

	@Override
	public <T extends AbstractPersistentObject> Optional<EntityReadAccess<T>> entityOptional(String alias) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<EntityStore<T>> opt = (Optional) store.storeOptional(alias);
		return opt.map(e -> new EntityReadAccessImpl<>(e));
	}

	@Override
	public BeanStoreMetaInfo meta() {
		return new BeanStoreMetaInfoImpl(store);
	}

	@Override
	public Iterator<EntityReadAccess<?>> iterator() {
		var baseIterator = store.iterator();
		return new Iterator<EntityReadAccess<?>> () {

			@Override
			public boolean hasNext() {
				return baseIterator.hasNext();
			}

			@Override
			public EntityReadAccess<?> next() {
				return new EntityReadAccessImpl<>(baseIterator.next());
			}
			
		};
	}

}
