package de.protubero.beanstore.impl;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import de.protubero.beanstore.api.BeanStoreMetaInfo;
import de.protubero.beanstore.api.BeanStoreSnapshot;
import de.protubero.beanstore.api.EntityStoreSnapshot;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.linksandlabels.Links;
import de.protubero.beanstore.store.EntityStore;
import de.protubero.beanstore.store.EntityStoreSet;

public class BeanStoreSnapshotImpl implements BeanStoreSnapshot {

	private EntityStoreSet<?> store;


	public BeanStoreSnapshotImpl(EntityStoreSet<?> store) {
		this.store = Objects.requireNonNull(store);
	}


	@Override
	public <T extends AbstractEntity> Optional<EntityStoreSnapshot<T>> entityOptional(Class<T> aClass) {
		return store.storeOptional(aClass).map(e -> new EntityStoreSnapshotImpl<>(e));
	}

	@Override
	public <T extends AbstractPersistentObject> Optional<EntityStoreSnapshot<T>> entityOptional(String alias) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<EntityStore<T>> opt = (Optional) store.storeOptional(alias);
		return opt.map(e -> new EntityStoreSnapshotImpl<>(e));
	}

	@Override
	public BeanStoreMetaInfo meta() {
		return new BeanStoreMetaInfoImpl(store);
	}

	@Override
	public Iterator<EntityStoreSnapshot<?>> iterator() {
		var baseIterator = store.iterator();
		return new Iterator<EntityStoreSnapshot<?>> () {

			@Override
			public boolean hasNext() {
				return baseIterator.hasNext();
			}

			@Override
			public EntityStoreSnapshot<?> next() {
				return new EntityStoreSnapshotImpl<>(baseIterator.next());
			}
			
		};
	}


	@Override
	public int version() {
		return store.version();
	}


	@Override
	public Links links() {
		return store.links();
	}

}
