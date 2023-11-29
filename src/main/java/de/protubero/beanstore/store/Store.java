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
import de.protubero.beanstore.base.entity.Companion;
import de.protubero.beanstore.base.entity.EntityCompanion;
import de.protubero.beanstore.base.entity.MapObject;
import de.protubero.beanstore.base.entity.MapObjectCompanion;

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
			storeByAliasMap.put(es.getCompanion().alias(), es);
			if (es.getCompanion().isBean()) {
				storeByClassMap.put(es.getCompanion().entityClass(), es);
			}	
		});
	}
	
	public Store() {
	}
	
	public EntityStore<MapObject> createMapStore(String alias) {
		return register(new MapObjectCompanion(alias));
	}

	public void removeMapStore(EntityStore<?> es) {
		if (!(es.getCompanion() instanceof MapObjectCompanion)) {
			throw new AssertionError();
		}
		storeByAliasMap.remove(es.getCompanion().alias());
	}
	
	
	public <X extends AbstractEntity> EntityStore<X> createBeanStore(Class<X> aClass) {
		return register(new EntityCompanion<>(aClass));
	}
	
	public <X extends AbstractEntity> EntityStore<X> transformOrCreateBeanStore(EntityCompanion<X> beanCompanion, Consumer<X> callback) {
		EntityStore<?> origEntityStore = storeByAliasMap.remove(beanCompanion.alias());
		if (origEntityStore != null) {
			if (!(origEntityStore.getCompanion() instanceof MapObjectCompanion)) {
				throw new StoreException("store with name " + beanCompanion.alias() + " is not a map store");
			}
		}	
		
		boolean isTaggedEntity = AbstractTaggedEntity.class.isAssignableFrom(beanCompanion.beanClass());
		
		// do the real conversion
		EntityStore<X> newEntityStore = register(beanCompanion);
		
		if (origEntityStore != null) {
			// copy instances
			origEntityStore.objects().forEach(obj -> {
				X newInstance = newEntityStore.newInstance();
				newInstance.id(obj.id());
				// copy all properties
				beanCompanion.transferProperties(obj, newInstance);
				
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
	
	public <X extends AbstractPersistentObject> EntityStore<X> register(Companion<X> companion) {
		EntityStore<X> store = new EntityStore<>(companion);

		log.info("registering store entity " + companion.alias());
		storeList.add(store);
		
		if (storeByAliasMap.put(companion.alias(), store) != null) {
			throw new RuntimeException("duplicate alias");
		}

		if (companion.entityClass() != MapObject.class) {
			if (storeByClassMap.put(companion.entityClass(), store) != null) {
				throw new RuntimeException("duplicate entity class: " + companion.entityClass());
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
