package de.protubero.beanstore.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.api.EntityStoreSnapshot;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.linksandlabels.Links;
import de.protubero.beanstore.store.EntityStore;

public class EntityStoreSnapshotImpl<T extends AbstractPersistentObject> implements EntityStoreSnapshot<T> {

	private EntityStore<T> store;

	EntityStoreSnapshotImpl(EntityStore<T> store) {
		this.store = Objects.requireNonNull(store);
	}

	@Override
	public BeanStoreEntity<T> meta() {
		return store.companion();
	}

	@Override
	public T find(long id) {
		return store.get(id);
	}

	@Override
	public Optional<T> findOptional(long id) {
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
