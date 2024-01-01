package de.protubero.beanstore.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.protubero.beanstore.api.BeanStoreMetaInfo;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.store.EntityStore;
import de.protubero.beanstore.store.EntityStoreSet;

public class BeanStoreMetaInfoImpl implements BeanStoreMetaInfo {

	private EntityStoreSet<?> store;

	BeanStoreMetaInfoImpl(EntityStoreSet<?> store) {
		this.store = Objects.requireNonNull(store);
	}

	@Override
	public Stream<BeanStoreEntity<?>> stream() {
		return StreamSupport.stream(store.spliterator(), false).map(es -> es.companion());
	}

	@Override
	public <T extends AbstractEntity> Optional<BeanStoreEntity<T>> entityOptional(Class<T> entityClass) {
		return store.storeOptional(entityClass).map(es -> es.companion());
	}

	@Override
	public <T extends AbstractPersistentObject> Optional<BeanStoreEntity<T>> entityOptional(String alias) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<EntityStore<T>> opt = (Optional) store.storeOptional(alias);
		return opt.map(es -> es.companion());
	}

}
