package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanStoreEntity;
import de.protubero.beanstore.base.InstanceRef;

public interface BeanStoreReader {

			
	Optional<BeanStoreEntity<?>> entity(String alias);

	<X extends AbstractEntity> Optional<BeanStoreEntity<X>> entity(Class<X> entityClass);
	
	Collection<BeanStoreEntity<?>> entities();
	
	
	default <T extends AbstractPersistentObject> T find(InstanceRef ref) {
		return find(ref.alias(), ref.id());
	}

	default <T extends AbstractEntity> T find(T ref) {
		return find(ref.alias(), ref.id());
	}
	
	default <T extends AbstractPersistentObject> Optional<T> findOptional(InstanceRef ref) {
		return findOptional(ref.alias(), ref.id());
	}
	
	
	<T extends AbstractPersistentObject> T find(String alias, Long id);

	<T extends AbstractEntity> T find(Class<T> aClass, Long id);
	
	<T extends AbstractPersistentObject> Optional<T> findOptional(String alias, Long id);

	<T extends AbstractEntity> Optional<T> findOptional(Class<T> aClass, Long id);
	
	<T extends AbstractPersistentObject> Stream<T> objects(String alias);

	boolean exists(String alias);
	
	<T extends AbstractEntity> Stream<T> objects(Class<T> aClass);
	
	default List<AbstractPersistentObject> resolveExisting(Iterable<? extends InstanceRef> refList) {
		List<AbstractPersistentObject> result = new ArrayList<>();
		for (InstanceRef ref : refList) {
			findOptional(ref).ifPresent(obj -> result.add(obj));
		}
		
		return result;
	}
	
	default List<AbstractPersistentObject> resolve(Iterable<? extends InstanceRef> refList) {
		List<AbstractPersistentObject> result = new ArrayList<>();
		for (InstanceRef ref : refList) {
			result.add(findOptional(ref).orElseThrow(() -> new StoreException("invalid ref " + ref.toRefString())));
		}
		
		return result;
	}
	
	BeanStoreReader snapshot();

	
}
