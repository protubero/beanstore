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

				
	BeanStoreMetaInfo meta();
	
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> EntityReadAccess<T> entity(String alias) {
		return (EntityReadAccess<T>) entityOptional(alias).orElseThrow(() -> {
			throw new BeanStoreException("invalid alias");
		});
	}

	default <T extends AbstractEntity> EntityReadAccess<T> entity(Class<T> aClass) {
		return entityOptional(aClass).orElseThrow(() -> {
			throw new BeanStoreException("invalid alias");
		});
	}
	
	<T extends AbstractPersistentObject> Optional<EntityReadAccess<T>> entityOptional(String alias);

	<T extends AbstractEntity> Optional<EntityReadAccess<T>> entityOptional(Class<T> aClass);
	
	
	
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

	
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> T find(InstanceKey key) {
		return (T) entity(key.alias()).find(key.id());
	}

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> T find(T ref) {
		return (T) entity(ref.getClass()).find(ref.id());
	}
	
	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> Optional<T> findOptional(InstanceKey key) {
		return (Optional<T>) entity(key.alias()).findOptional(key.id());
	}
	
	
	
	default List<AbstractPersistentObject> resolveExisting(Iterable<? extends InstanceKey> keyList) {
		return resolve(keyList, null);
	}
	
	default List<AbstractPersistentObject> resolveAll(Iterable<? extends InstanceKey> keyList) {
		return resolve(keyList, key -> {throw new BeanStoreException("invalid key " + key.toKeyString());});
	}
	
	/**
	 * Create an immutable snapshot of the store.
	 * 
	 */
	BeanStoreReadAccess snapshot();

	
}
