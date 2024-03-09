package de.protubero.beanstore.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreException;
import de.protubero.beanstore.entity.MapObject;
import de.protubero.beanstore.keys.PersistentObjectKey;

/**
 * BeanStore read operations. 
 *
 */
public interface BeanStoreSnapshot extends Iterable<EntityStoreSnapshot<?>> {

	int version();
	
	/**
	 * Entity meta information 
	 */
	BeanStoreMetaInfo meta();

	/**
	 * Access read operations of a single entity with the given <i>alias</i><br>
	 * Throws a BeanStoreException if the alias is invalid. 
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> EntityStoreSnapshot<T> entity(String alias) {
		return (EntityStoreSnapshot<T>) entityOptional(alias).orElseThrow(() -> {
			throw new BeanStoreException("invalid alias");
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default EntityStoreSnapshot<MapObject> mapEntity(String alias) {
		EntityStoreSnapshot<? extends AbstractPersistentObject> e = entity(alias);
		if (e.meta().isBean()) {
			throw new BeanStoreException("not a map entity: " + alias);
		}
		return (EntityStoreSnapshot<MapObject>) e;
	}
	
	/**
	 * Access read operations of a single entity with the given Java Bean class.<br> 
	 * Throws a BeanStoreException if the parameter is invalid. 
	 */
	default <T extends AbstractEntity> EntityStoreSnapshot<T> entity(Class<T> aClass) {
		return entityOptional(aClass).orElseThrow(() -> {
			throw new BeanStoreException("invalid bean class: " + aClass.getName());
		});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default <T extends AbstractPersistentObject> EntityStoreSnapshot<T> entity(PersistentObjectKey<T> key) {
		if (key.entityClass() != null) {
			// Please call me if you know why this Cast is necessary??
			return entity((Class) key.entityClass());
		} else {
			return entity(key.alias());
		}
	}
	
	
	/**
	 * Access read operations of a single entity with the given <i>alias</i>
	 */
	<T extends AbstractPersistentObject> Optional<EntityStoreSnapshot<T>> entityOptional(String alias);

	/**
	 * Access read operations of a single entity with the given Java Bean class. 
	 */
	<T extends AbstractEntity> Optional<EntityStoreSnapshot<T>> entityOptional(Class<T> aClass);
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default Optional<EntityStoreSnapshot<MapObject>> mapEntityOptional(String alias) {
		Optional<EntityStoreSnapshot<AbstractPersistentObject>> e = entityOptional(alias);
		if (e.get().meta().isBean()) {
			throw new BeanStoreException("not a map entity: " + alias);
		}
		return (Optional) e;
	}


	/**
	 * Find an instance determined by an instance reference.<br>
	 * Throws a BeanStoreException if the reference is invalid (either alias or id)
	 */
	default <T extends AbstractPersistentObject> T find(PersistentObjectKey<T> key) {
		Objects.requireNonNull(key);
		
		return (T) entity(key).find(key.id());
	}

	/**
	 * Find the <b>current</b> instance determined by an instance. The result might even be the instance itself.<br>
	 * Throws a BeanStoreException if the reference is invalid (either alias or id).
	 */
//	@SuppressWarnings("unchecked")
//	default <T extends AbstractPersistentObject> T find(T ref) {
//		Objects.requireNonNull(ref);
//		if (ref.companion() == null) {
//			throw new BeanStoreException("Instance was not created by the store");
//		}
//		return (T) entity(ref.alias()).find(ref.id());
//	}
	
	/**
	 * Find an instance determined by an instance reference.<br>
	 */
	default <T extends AbstractPersistentObject> Optional<T> findOptional(PersistentObjectKey<T> key) {
		Objects.requireNonNull(key);
		return (Optional<T>) entity(key).findOptional(key.id());
	}
	

	
	/**
	 * Resolve a list of instance references into a list of instances. The <i>missingKeyConsumer</i>
	 * is notified for each reference which cannot be resolved. 
	 */
	default List<AbstractPersistentObject> resolve(Iterable<? extends PersistentObjectKey<?>> keyList, 
			Consumer<PersistentObjectKey<?>> missingKeyConsumer) {
		List<AbstractPersistentObject> result = new ArrayList<>();
		for (PersistentObjectKey<?> key : keyList) {
			AbstractPersistentObject obj = entity(key).find(key);
			if (obj == null) {
				if (missingKeyConsumer != null) {
					missingKeyConsumer.accept(key);
				}
			} else {
				result.add(obj);
			}
		}
		
		return result;
	}
	
	
	/**
	 * Resolve a list of instance references into a list of instances.
	 * Non-resolve references are ignored.	
	 */
	default List<AbstractPersistentObject> resolveExisting(Iterable<? extends PersistentObjectKey<?>> keyList) {
		return resolve(keyList, null);
	}
	/**
	 * Resolve a list of instance references into a list of instances.
	 * Non-resolve references lead to a BeanStoreException.	
	 */
	default List<AbstractPersistentObject> resolve(Iterable<? extends PersistentObjectKey<?>> keyList) {
		return resolve(keyList, key -> {throw new BeanStoreException("invalid key " + key.toString());});
	}

	
	
}
