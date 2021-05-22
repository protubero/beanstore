package de.protubero.beanstore.store;

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
	
	
	<T extends AbstractPersistentObject> T find(InstanceRef ref);

	<T extends AbstractEntity> T find(T ref);
	
	<T extends AbstractPersistentObject> Optional<T> findOptional(InstanceRef ref);
	
	<T extends AbstractPersistentObject> T find(String alias, Long id);

	<T extends AbstractEntity> T find(Class<T> aClass, Long id);
	
	<T extends AbstractPersistentObject> Optional<T> findOptional(String alias, Long id);

	<T extends AbstractEntity> Optional<T> findOptional(Class<T> aClass, Long id);
	
	<T extends AbstractPersistentObject> Stream<T> objects(String alias);

	boolean exists(String alias);
	
	<T extends AbstractEntity> Stream<T> objects(Class<T> aClass);
	
	List<AbstractPersistentObject> resolveExisting(Iterable<? extends InstanceRef> refList);
	
	List<AbstractPersistentObject> resolve(Iterable<? extends InstanceRef> refList);
	
	BeanStoreReader snapshot();

	
}
