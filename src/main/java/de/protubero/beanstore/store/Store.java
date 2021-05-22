package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanStoreEntity;
import de.protubero.beanstore.base.AbstractPersistentObject.Transition;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.base.Compagnon;
import de.protubero.beanstore.base.EntityCompagnon;
import de.protubero.beanstore.base.EntityMap;
import de.protubero.beanstore.base.EntityMapCompagnon;
import de.protubero.beanstore.base.InstanceRef;
import de.protubero.beanstore.writer.Transaction;

public class Store implements InstanceFactory, BeanStoreReader {


	
	public static final Logger log = LoggerFactory.getLogger(Store.class);
	
	
	private Map<String, EntityStore<?>> storeByAliasMap = new HashMap<>();
	private Map<Class<?>, EntityStore<?>> storeByClassMap = new HashMap<>();
		
	
	public EntityStore<EntityMap> createMapStore(String alias) {
		return register(new EntityMapCompagnon(alias));
	}

	public void removeMapStore(EntityStore<?> es) {
		if (!(es.getCompagnon() instanceof EntityMapCompagnon)) {
			throw new AssertionError();
		}
		storeByAliasMap.remove(es.getCompagnon().alias());
	}
	
	
	public <X extends AbstractEntity> EntityStore<X> createBeanStore(Class<X> aClass) {
		return register(new EntityCompagnon<>(aClass));
	}
	
	/**
	 * Transforms an existing map store to a bean store
	 * 
	 * @param <X>
	 * @param aClass
	 * @return
	 */
	public <X extends AbstractEntity> EntityStore<X> transformOrCreateBeanStore(EntityCompagnon<X> beanCompagnon, Consumer<X> callback) {
		EntityStore<?> origEntityStore = storeByAliasMap.remove(beanCompagnon.alias());
		if (origEntityStore != null) {
			if (!(origEntityStore.getCompagnon() instanceof EntityMapCompagnon)) {
				throw new StoreException("store with name " + beanCompagnon.alias() + " is not a map store");
			}
		}	
		
		// do the real conversion
		EntityStore<X> newEntityStore = register(beanCompagnon);
		
		if (origEntityStore != null) {
			// copy instances
			origEntityStore.objects().forEach(obj -> {
				X newInstance = newEntityStore.newInstance();
				newInstance.id(obj.id());
				// copy all properties
				beanCompagnon.transferProperties(obj, newInstance);
				newInstance.applyTransition(Transition.INSTANTIATED_TO_READY);
				
				if (callback != null) {
					callback.accept(newInstance);
				}
				newEntityStore.put(newInstance);
			});
		}	
		
		return newEntityStore;
	}
	
	public <X extends AbstractPersistentObject> EntityStore<X> register(Compagnon<X> compagnon) {
		EntityStore<X> store = new EntityStore<>(compagnon);

		log.info("registering store entity " + compagnon.alias());
		
		if (storeByAliasMap.put(compagnon.alias(), store) != null) {
			throw new RuntimeException("duplicate alias");
		}

		if (compagnon.entityClass() != EntityMap.class) {
			if (storeByClassMap.put(compagnon.entityClass(), store) != null) {
				throw new RuntimeException("duplicate entity class: " + compagnon.entityClass());
			}
		}
		
		return store;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends AbstractPersistentObject> EntityStore<T> store(String alias) {
		EntityStore<T> result = (EntityStore) storeByAliasMap.get(alias);
		if (result == null) {			
			throw new RuntimeException("unknown store: " + alias);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractEntity> EntityStore<T> store(Class<T> aClass) {
		EntityStore<T> result = (EntityStore<T>) storeByClassMap.get(aClass);
		if (result == null) {			
			throw new RuntimeException("unknown store: " + aClass);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends AbstractPersistentObject> Optional<EntityStore<T>> storeOptional(String alias) {
		EntityStore<T> result = (EntityStore) storeByAliasMap.get(alias);
		return Optional.ofNullable(result);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractEntity> Optional<EntityStore<T>> storeOptional(Class<T> aClass) {
		EntityStore<T> result = (EntityStore<T>) storeByClassMap.get(aClass);
		return Optional.ofNullable(result);
	}
	
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> T newInstance(String alias) {
		return ((EntityStore<T>) store(alias)).newInstance();
	}
	
	
	@Override
	public <T extends AbstractEntity> T newInstance(Class<T> aClass) {
		return ((EntityStore<T>) store(aClass)).newInstance();
	}
	
	public Iterable<EntityStore<?>> entityStores() {
		return storeByAliasMap.values();
	}
		

	@Override
	public List<AbstractPersistentObject> resolveExisting(Iterable<? extends InstanceRef> refList) {
		List<AbstractPersistentObject> result = new ArrayList<>();
		for (InstanceRef ref : refList) {
			findOptional(ref).ifPresent(obj -> result.add(obj));
		}
		
		return result;
	}
	
	@Override
	public List<AbstractPersistentObject> resolve(Iterable<? extends InstanceRef> refList) {
		List<AbstractPersistentObject> result = new ArrayList<>();
		for (InstanceRef ref : refList) {
			result.add(findOptional(ref).orElseThrow(() -> new StoreException("invalid ref " + ref.toRefString())));
		}
		
		return result;
	}
	
	
	@Override
	public <T extends AbstractPersistentObject> T find(InstanceRef ref) {
		return find(ref.alias(), ref.id());
	}

	@Override
	public <T extends AbstractEntity> T find(T ref) {
		return find(ref.alias(), ref.id());
	}
	
	@Override
	public <T extends AbstractPersistentObject> Optional<T> findOptional(InstanceRef ref) {
		return findOptional(ref.alias(), ref.id());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractPersistentObject> T find(String alias, Long id) {
		EntityStore<?> entityStore = store(alias);
		return (T) entityStore.get(Objects.requireNonNull(id));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractEntity> T find(Class<T> aClass, Long id) {
		EntityStore<?> entityStore = store(aClass);
		return (T) entityStore.get(Objects.requireNonNull(id));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractPersistentObject> Optional<T> findOptional(String alias, Long id) {
		EntityStore<?> entityStore = store(alias);
		return (Optional<T>) entityStore.getOptional(Objects.requireNonNull(id));
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractEntity> Optional<T> findOptional(Class<T> aClass, Long id) {
		EntityStore<?> entityStore = store(aClass);
		return (Optional<T>) entityStore.getOptional(id);
	}

	public void applyInstanceStateTransition(Transition transition) {
		for (EntityStore<?> eStore : entityStores()) {
			eStore.objects().forEach(instance -> instance.applyTransition(transition));
		}
	}

	public boolean empty() {
		return storeByAliasMap.values().stream().noneMatch(s -> s.size() > 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractPersistentObject> Stream<T> objects(String alias) {
		return (Stream<T>) store(alias).objects();
	}

	@Override
	public <T extends AbstractEntity> Stream<T> objects(Class<T> aClass) {
		return (Stream<T>) store(aClass).objects();
	}

	@Override
	public boolean exists(String alias) {
		return storeByAliasMap.containsKey(alias);
	}

	@Override
	public Optional<BeanStoreEntity<?>> entity(String alias) {
		return Optional.ofNullable(storeByAliasMap.get(alias)).map(es -> es.getCompagnon());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractEntity> Optional<BeanStoreEntity<T>> entity(Class<T> aClass) {
		return Optional.ofNullable((EntityStore<T>) storeByClassMap.get(aClass)).map(es -> es.getCompagnon());
	}

	@Override
	public Collection<BeanStoreEntity<?>> entities() {
		List<BeanStoreEntity<?>> result = new ArrayList<>();
		storeByAliasMap.values().forEach(es -> {
			result.add(es.getCompagnon());
		});
		return Collections.unmodifiableList(result);
	}

	@SuppressWarnings("unchecked")
	public BeanStoreReader snapshot() {
		Map<String, Collection<? extends AbstractPersistentObject>> storeSnapshot = new HashMap<>();
		for (EntityStore<?> es : entityStores()) {
			storeSnapshot.put(es.getCompagnon().alias(), (Collection<AbstractPersistentObject>) es.values());
		}
		return new StoreSnapshot(this, storeSnapshot);
	}


	
}
