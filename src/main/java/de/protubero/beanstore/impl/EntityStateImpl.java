package de.protubero.beanstore.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.api.EntityState;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.store.EntityStore;

public class EntityStateImpl<T extends AbstractPersistentObject> implements EntityState<T> {

	private EntityStore<T> store;

	EntityStateImpl(EntityStore<T> store) {
		this.store = Objects.requireNonNull(store);
	}

	@Override
	public BeanStoreEntity<T> meta() {
		return store.companion();
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
	public int count() {
		return store.size();
	}

}
