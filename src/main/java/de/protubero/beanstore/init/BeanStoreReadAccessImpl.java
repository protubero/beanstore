package de.protubero.beanstore.init;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanStoreEntity;
import de.protubero.beanstore.store.BeanStoreMetaInfo;
import de.protubero.beanstore.store.BeanStoreReadAccess;
import de.protubero.beanstore.store.EntityReadAccess;
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
