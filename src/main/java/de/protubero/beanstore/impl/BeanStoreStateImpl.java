package de.protubero.beanstore.impl;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import de.protubero.beanstore.api.BeanStoreMetaInfo;
import de.protubero.beanstore.api.BeanStoreState;
import de.protubero.beanstore.api.EntityState;
import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.store.EntityStore;
import de.protubero.beanstore.store.EntityStoreSet;

public class BeanStoreStateImpl implements BeanStoreState {

	private EntityStoreSet<?> store;

	public BeanStoreStateImpl(EntityStoreSet<?> store) {
		this.store = Objects.requireNonNull(store);
	}


	@Override
	public <T extends AbstractEntity> Optional<EntityState<T>> entityOptional(Class<T> aClass) {
		return store.storeOptional(aClass).map(e -> new EntityStateImpl<>(e));
	}

	@Override
	public <T extends AbstractPersistentObject> Optional<EntityState<T>> entityOptional(String alias) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<EntityStore<T>> opt = (Optional) store.storeOptional(alias);
		return opt.map(e -> new EntityStateImpl<>(e));
	}

	@Override
	public BeanStoreMetaInfo meta() {
		return new BeanStoreMetaInfoImpl(store);
	}

	@Override
	public Iterator<EntityState<?>> iterator() {
		var baseIterator = store.iterator();
		return new Iterator<EntityState<?>> () {

			@Override
			public boolean hasNext() {
				return baseIterator.hasNext();
			}

			@Override
			public EntityState<?> next() {
				return new EntityStateImpl<>(baseIterator.next());
			}
			
		};
	}

}
