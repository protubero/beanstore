package de.protubero.beanstore.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.protubero.beanstore.api.BeanStoreMetaInfo;
import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.BeanStoreEntity;
import de.protubero.beanstore.store.EntityStore;
import de.protubero.beanstore.store.Store;

public class BeanStoreMetaInfoImpl implements BeanStoreMetaInfo {

	private Store store;

	BeanStoreMetaInfoImpl(Store store) {
		this.store = Objects.requireNonNull(store);
	}

	@Override
	public Stream<BeanStoreEntity<?>> stream() {
		return StreamSupport.stream(store.entityStores().spliterator(), false).map(es -> es.getCompanion());
	}

	@Override
	public <T extends AbstractEntity> Optional<BeanStoreEntity<T>> entityOptional(Class<T> entityClass) {
		return store.storeOptional(entityClass).map(es -> es.getCompanion());
	}

	@Override
	public <T extends AbstractPersistentObject> Optional<BeanStoreEntity<T>> entityOptional(String alias) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<EntityStore<T>> opt = (Optional) store.storeOptional(alias);
		return opt.map(es -> es.getCompanion());
	}

}
