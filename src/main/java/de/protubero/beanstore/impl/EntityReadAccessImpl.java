package de.protubero.beanstore.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.api.EntityReadAccess;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.BeanStoreEntity;
import de.protubero.beanstore.store.EntityStore;

public class EntityReadAccessImpl<T extends AbstractPersistentObject> implements EntityReadAccess<T> {

	private EntityStore<T> store;

	EntityReadAccessImpl(EntityStore<T> store) {
		this.store = Objects.requireNonNull(store);
	}

	@Override
	public BeanStoreEntity<T> meta() {
		return store.getCompanion();
	}

	@Override
	public T find(Long id) {
		return store.get(id);
	}

	@Override
	public Optional<T> findOptional(Long id) {
		return store.getOptional(id);
	}

	@Override
	public Stream<T> stream() {
		return store.objects();
	}

	@Override
	public EntityReadAccess<T> snapshot() {
		return new EntityReadAccessImpl<>(store.cloneStore());
	}

	@Override
	public int count() {
		return store.size();
	}

}
