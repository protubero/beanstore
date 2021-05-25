package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanStoreEntity;
import de.protubero.beanstore.base.InstanceKey;

/**
 * BeanStore read operations. 
 *
 */
public interface BeanStoreReadAccess {

			
	Optional<BeanStoreEntity<?>> entity(String alias);

	<X extends AbstractEntity> Optional<BeanStoreEntity<X>> entity(Class<X> entityClass);
	
	Collection<BeanStoreEntity<?>> entities();
	
	default <T extends AbstractPersistentObject> T find(InstanceKey key) {
		return find(key.alias(), key.id());
	}

	default <T extends AbstractEntity> T find(T ref) {
		return find(ref.alias(), ref.id());
	}
	
	default <T extends AbstractPersistentObject> Optional<T> findOptional(InstanceKey key) {
		return findOptional(key.alias(), key.id());
	}
	
	
	<T extends AbstractPersistentObject> T find(String alias, Long id);

	<T extends AbstractEntity> T find(Class<T> aClass, Long id);
	
	<T extends AbstractPersistentObject> Optional<T> findOptional(String alias, Long id);

	<T extends AbstractEntity> Optional<T> findOptional(Class<T> aClass, Long id);
	
	<T extends AbstractPersistentObject> Stream<T> objects(String alias);

	boolean exists(String alias);
	
	<T extends AbstractEntity> Stream<T> objects(Class<T> aClass);

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
	
	
	default List<AbstractPersistentObject> resolveExisting(Iterable<? extends InstanceKey> keyList) {
		return resolve(keyList, null);
	}
	
	default List<AbstractPersistentObject> resolveAll(Iterable<? extends InstanceKey> keyList) {
		return resolve(keyList, key -> {throw new StoreException("invalid key " + key.toKeyString());});
	}
	
	BeanStoreReadAccess snapshot();

	
}