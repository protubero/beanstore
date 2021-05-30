package de.protubero.beanstore.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.AbstractPersistentObject.Transition;
import de.protubero.beanstore.base.Compagnon;
import de.protubero.beanstore.base.EntityCompagnon;
import de.protubero.beanstore.base.MapObject;
import de.protubero.beanstore.base.MapObjectCompagnon;

public class Store implements InstanceFactory, Iterable<EntityStore<?>> {

	
	public static final Logger log = LoggerFactory.getLogger(Store.class);
		
	private Map<String, EntityStore<?>> storeByAliasMap = new HashMap<>();
	private Map<Class<?>, EntityStore<?>> storeByClassMap = new HashMap<>();
		
	/**
	 * Copy constructor
	 * 
	 * @param entityStores
	 */
	private Store(Iterable<EntityStore<?>> entityStores) {
		entityStores.forEach(es -> {
			storeByAliasMap.put(es.getCompagnon().alias(), es);
			if (es.getCompagnon().isBean()) {
				storeByClassMap.put(es.getCompagnon().entityClass(), es);
			}	
		});
	}
	
	public Store() {
	}
	
	public EntityStore<MapObject> createMapStore(String alias) {
		return register(new MapObjectCompagnon(alias));
	}

	public void removeMapStore(EntityStore<?> es) {
		if (!(es.getCompagnon() instanceof MapObjectCompagnon)) {
			throw new AssertionError();
		}
		storeByAliasMap.remove(es.getCompagnon().alias());
	}
	
	
	public <X extends AbstractEntity> EntityStore<X> createBeanStore(Class<X> aClass) {
		return register(new EntityCompagnon<>(aClass));
	}
	
	public <X extends AbstractEntity> EntityStore<X> transformOrCreateBeanStore(EntityCompagnon<X> beanCompagnon, Consumer<X> callback) {
		EntityStore<?> origEntityStore = storeByAliasMap.remove(beanCompagnon.alias());
		if (origEntityStore != null) {
			if (!(origEntityStore.getCompagnon() instanceof MapObjectCompagnon)) {
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

		if (compagnon.entityClass() != MapObject.class) {
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
	
		
//	@SuppressWarnings("unchecked")
//	public <T extends AbstractPersistentObject> T find(String alias, Long id) {
//		EntityStore<?> entityStore = store(alias);
//		return (T) entityStore.get(Objects.requireNonNull(id));
//	}
//
//	@SuppressWarnings("unchecked")
//	public <T extends AbstractEntity> T find(Class<T> aClass, Long id) {
//		EntityStore<?> entityStore = store(aClass);
//		return (T) entityStore.get(Objects.requireNonNull(id));
//	}
//	
//	@SuppressWarnings("unchecked")
//	public <T extends AbstractPersistentObject> Optional<T> findOptional(String alias, Long id) {
//		EntityStore<?> entityStore = store(alias);
//		return (Optional<T>) entityStore.getOptional(Objects.requireNonNull(id));
//	}
//
//	@SuppressWarnings("unchecked")
//	public <T extends AbstractEntity> Optional<T> findOptional(Class<T> aClass, Long id) {
//		EntityStore<?> entityStore = store(aClass);
//		return (Optional<T>) entityStore.getOptional(id);
//	}

	public void applyInstanceStateTransition(Transition transition) {
		for (EntityStore<?> eStore : entityStores()) {
			eStore.objects().forEach(instance -> instance.applyTransition(transition));
		}
	}

	public boolean empty() {
		return storeByAliasMap.values().stream().noneMatch(s -> s.size() > 0);
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public <T extends AbstractPersistentObject> Stream<T> objects(String alias) {
//		return (Stream<T>) store(alias).objects();
//	}
//
//	@Override
//	public <T extends AbstractEntity> Stream<T> stream(Class<T> aClass) {
//		return (Stream<T>) store(aClass).objects();
//	}
//
//
//	public Optional<BeanStoreEntity<?>> entity(String alias) {
//		return Optional.ofNullable(storeByAliasMap.get(alias)).map(es -> es.getCompagnon());
//	}
//
//	@SuppressWarnings("unchecked")
//	public <T extends AbstractEntity> Optional<BeanStoreEntity<T>> entity(Class<T> aClass) {
//		return Optional.ofNullable((EntityStore<T>) storeByClassMap.get(aClass)).map(es -> es.getCompagnon());
//	}
//
//	public Collection<BeanStoreEntity<?>> entities() {
//		List<BeanStoreEntity<?>> result = new ArrayList<>();
//		storeByAliasMap.values().forEach(es -> {
//			result.add(es.getCompagnon());
//		});
//		return Collections.unmodifiableList(result);
//	}
//
//	public BeanStoreReadAccess snapshot() {
//		List<EntityStore<?>> resultStores = new ArrayList<>();
//		storeByAliasMap.values().forEach(es -> {
//			resultStores.add(es.cloneStore());
//		});
//		
//		return new Store(resultStores);
//	}

	
}
