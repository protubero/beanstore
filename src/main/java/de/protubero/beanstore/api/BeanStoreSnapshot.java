package de.protubero.beanstore.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreException;
import de.protubero.beanstore.entity.InstanceKey;
import de.protubero.beanstore.entity.MapObject;

/**
 * BeanStore read operations. 
 *
 */
public interface BeanStoreSnapshot extends Iterable<EntityState<?>> {

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
	default <T extends AbstractPersistentObject> EntityState<T> entity(String alias) {
		return (EntityState<T>) entityOptional(alias).orElseThrow(() -> {
			throw new BeanStoreException("invalid alias");
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default EntityState<MapObject> mapEntity(String alias) {
		var e = entityOptional(alias);
		if (e.isEmpty()) {
			throw new BeanStoreException("invalid alias: " + alias);
		}
		if (e.get().meta().isBean()) {
			throw new BeanStoreException("not a map entity: " + alias);
		}
		return (EntityState) e.get();
	}

	
	/**
	 * Access read operations of a single entity with the given Java Bean class.<br> 
	 * Throws a BeanStoreException if the parameter is invalid. 
	 */
	default <T extends AbstractEntity> EntityState<T> entity(Class<T> aClass) {
		return entityOptional(aClass).orElseThrow(() -> {
			throw new BeanStoreException("invalid bean class: " + aClass.getName());
		});
	}
	
	/**
	 * Access read operations of a single entity with the given <i>alias</i>
	 */
	<T extends AbstractPersistentObject> Optional<EntityState<T>> entityOptional(String alias);

	/**
	 * Access read operations of a single entity with the given Java Bean class. 
	 */
	<T extends AbstractEntity> Optional<EntityState<T>> entityOptional(Class<T> aClass);
	
	
	/**
	 * Resolve a list of instance references into a list of instances. The <i>missingKeyConsumer</i>
	 * is notified for each reference which cannot be resolved. 
	 */
	default List<AbstractPersistentObject> resolve(Iterable<? extends InstanceKey> keyList, 
			Consumer<InstanceKey> missingKeyConsumer) {
		List<AbstractPersistentObject> result = new ArrayList<>();
		for (InstanceKey key : keyList) {
			Optional<AbstractPersistentObject> opt = findOptional(key);
			if (opt.isEmpty()) {
				if (missingKeyConsumer != null) {
					missingKeyConsumer.accept(key);
				}
			} else {
				result.add(opt.get());
			}
		}
		
		return result;
	}


	/**
	 * Find an instance determined by an instance reference.<br>
	 * Throws a BeanStoreException if the reference is invalid (either alias or id)
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> T find(InstanceKey key) {
		Objects.requireNonNull(key);
		
		if (key instanceof AbstractPersistentObject) {
			return (T) find((AbstractPersistentObject) key);
		}
		
		if (key.alias() == null || key.id() == null) {
			throw new BeanStoreException("Incomplete key");
		}
		return (T) entity(key.alias()).find(key.id());
	}

	/**
	 * Find the <b>current</b> instance determined by an instance. The result might even be the instance itself.<br>
	 * Throws a BeanStoreException if the reference is invalid (either alias or id).
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> T find(T ref) {
		Objects.requireNonNull(ref);
		if (ref.companion() == null) {
			throw new BeanStoreException("Instance was not created by the store");
		}
		return (T) entity(ref.alias()).find(ref.id());
	}
	
	/**
	 * Find an instance determined by an instance reference.<br>
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> Optional<T> findOptional(InstanceKey key) {
		Objects.requireNonNull(key);
		if (key.alias() == null || key.id() == null) {
			throw new BeanStoreException("Incomplete key");
		}
		return (Optional<T>) entity(key.alias()).findOptional(key.id());
	}
	
	/**
	 * Resolve a list of instance references into a list of instances.
	 * Non-resolve references are ignored.	
	 */
	default List<AbstractPersistentObject> resolveExisting(Iterable<? extends InstanceKey> keyList) {
		return resolve(keyList, null);
	}
	/**
	 * Resolve a list of instance references into a list of instances.
	 * Non-resolve references lead to a BeanStoreException.	
	 */
	default List<AbstractPersistentObject> resolveAll(Iterable<? extends InstanceKey> keyList) {
		return resolve(keyList, key -> {throw new BeanStoreException("invalid key " + key.toKeyString());});
	}
	
	
}
