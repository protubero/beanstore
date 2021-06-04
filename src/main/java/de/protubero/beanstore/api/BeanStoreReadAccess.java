package de.protubero.beanstore.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.BeanStoreException;
import de.protubero.beanstore.base.entity.InstanceKey;

/**
 * BeanStore read operations. 
 *
 */
public interface BeanStoreReadAccess extends Iterable<EntityReadAccess<?>> {

	/**
	 * Entity meta information 
	 */
	BeanStoreMetaInfo meta();
	
	/**
	 * Access read operations of a single entity with the given <i>alias</i><br>
	 * Throws a BeanStoreException if the alias is invalid. 
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> EntityReadAccess<T> entity(String alias) {
		return (EntityReadAccess<T>) entityOptional(alias).orElseThrow(() -> {
			throw new BeanStoreException("invalid alias");
		});
	}

	/**
	 * Access read operations of a single entity with the given Java Bean class.<br> 
	 * Throws a BeanStoreException if the parameter is invalid. 
	 */
	default <T extends AbstractEntity> EntityReadAccess<T> entity(Class<T> aClass) {
		return entityOptional(aClass).orElseThrow(() -> {
			throw new BeanStoreException("invalid alias");
		});
	}
	
	/**
	 * Access read operations of a single entity with the given <i>alias</i>
	 */
	<T extends AbstractPersistentObject> Optional<EntityReadAccess<T>> entityOptional(String alias);

	/**
	 * Access read operations of a single entity with the given Java Bean class. 
	 */
	<T extends AbstractEntity> Optional<EntityReadAccess<T>> entityOptional(Class<T> aClass);
	
	
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
		return (T) entity(key.alias()).find(key.id());
	}

	/**
	 * Find the <b>current</b> instance determined by an instance. The result might even be the instance itself.<br>
	 * Throws a BeanStoreException if the reference is invalid (either alias or id).
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> T find(T ref) {
		if (ref.alias() == null || ref.id() == null) {
			throw new BeanStoreException("Instance was not created by the store");
		}
		return (T) entity(ref.alias()).find(ref.id());
	}
	
	/**
	 * Find an instance determined by an instance reference.<br>
	 */
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> Optional<T> findOptional(InstanceKey key) {
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
	
	/**
	 * Returns an immutable snapshot of the store.
	 */
	BeanStoreReadAccess snapshot();
	
}
