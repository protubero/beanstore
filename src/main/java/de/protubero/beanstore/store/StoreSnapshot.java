package de.protubero.beanstore.store;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanStoreEntity;
import de.protubero.beanstore.base.InstanceRef;

class StoreSnapshot implements BeanStoreReader {

	private BeanStoreReader baseStore;
	private Map<String, Collection<? extends AbstractPersistentObject>> snapshotMap;
	
	public StoreSnapshot(BeanStoreReader baseStore, Map<String, Collection<? extends AbstractPersistentObject>> snapshotMap) {
		this.baseStore = Objects.requireNonNull(baseStore);
		this.snapshotMap = Objects.requireNonNull(snapshotMap);
	}

	@Override
	public Optional<BeanStoreEntity<?>> entity(String alias) {
		return baseStore.entity(alias);
	}

	@Override
	public <X extends AbstractEntity> Optional<BeanStoreEntity<X>> entity(Class<X> entityClass) {
		return baseStore.entity(entityClass);
	}

	@Override
	public Collection<BeanStoreEntity<?>> entities() {
		return baseStore.entities();
	}

	@Override
	public <T extends AbstractPersistentObject> T find(InstanceRef ref) {
		snapshotMap.get(ref.alias()).stream().filter(null)
		return null;
	}

	@Override
	public <T extends AbstractEntity> T find(T ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends AbstractPersistentObject> Optional<T> findOptional(InstanceRef ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends AbstractPersistentObject> T find(String alias, Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends AbstractEntity> T find(Class<T> aClass, Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends AbstractPersistentObject> Optional<T> findOptional(String alias, Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends AbstractEntity> Optional<T> findOptional(Class<T> aClass, Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractPersistentObject> Stream<T> objects(String alias) {
		return (Stream<T>) snapshotMap.get(alias).stream();
	}

	@Override
	public boolean exists(String alias) {
		return baseStore.exists(alias);
	}

	@Override
	public <T extends AbstractEntity> Stream<T> objects(Class<T> aClass) {
		return null;
	}

	@Override
	public List<AbstractPersistentObject> resolveExisting(Iterable<? extends InstanceRef> refList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AbstractPersistentObject> resolve(Iterable<? extends InstanceRef> refList) {
		return null;
	}

	@Override
	public BeanStoreReader snapshot() {
		return this;
	}

	
	
}
