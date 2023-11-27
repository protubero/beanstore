package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.Transition;
import de.protubero.beanstore.base.entity.AbstractTaggedEntity;
import de.protubero.beanstore.base.entity.Compagnon;
import de.protubero.beanstore.base.entity.EntityCompagnon;
import de.protubero.beanstore.base.entity.MapObject;
import de.protubero.beanstore.base.entity.MapObjectCompagnon;

public class Store implements InstanceFactory, Iterable<EntityStore<?>> {

	
	public static final Logger log = LoggerFactory.getLogger(Store.class);
		
	private Map<String, EntityStore<?>> storeByAliasMap = new HashMap<>();
	private Map<Class<?>, EntityStore<?>> storeByClassMap = new HashMap<>();
	private List<EntityStore<?>> storeList = new ArrayList<>();
		
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
		
		boolean isTaggedEntity = AbstractTaggedEntity.class.isAssignableFrom(beanCompagnon.beanClass());
		
		// do the real conversion
		EntityStore<X> newEntityStore = register(beanCompagnon);
		
		if (origEntityStore != null) {
			// copy instances
			origEntityStore.objects().forEach(obj -> {
				X newInstance = newEntityStore.newInstance();
				newInstance.id(obj.id());
				// copy all properties
				beanCompagnon.transferProperties(obj, newInstance);
				
				// set tags ref to entity
				if (isTaggedEntity) {
					((AbstractTaggedEntity) newInstance).getTags().setEntity((AbstractTaggedEntity) newInstance);
				}
				
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
		storeList.add(store);
		
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

	public <T extends AbstractPersistentObject> EntityStore<T> store(T ref) {
		// this should also work for instances which were not created by the store
		return store(AbstractPersistentObject.aliasOf(ref));
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
	

	public void applyInstanceStateTransition(Transition transition) {
		for (EntityStore<?> eStore : entityStores()) {
			eStore.objects().forEach(instance -> instance.applyTransition(transition));
		}
	}

	public boolean empty() {
		return storeList.stream().noneMatch(s -> s.size() > 0);
	}

	@Override
	public Iterator<EntityStore<?>> iterator() {
		return storeList.iterator();
	}


	public Store snapshot() {
		List<EntityStore<?>> resultStores = new ArrayList<>();
		entityStores().forEach(es -> {
			resultStores.add(es.cloneStore());
		});
		
		return new Store(resultStores);
	}

	@Override
	public Map<String, Object> extractProperties(AbstractPersistentObject apo) {
		return store(apo).extractProperties(apo);
	}
	
}
