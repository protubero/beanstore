package de.protubero.beanstore.store;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.Companion;

public interface EntityStore<T extends AbstractPersistentObject> {

	boolean isImmutable();
	
	EntityStoreSet<?> storeSet();
	
	T get(Long id);

	T getNullable(Long id);
	
	Optional<T> getOptional(Long id);

	Stream<T> objects();
	
	int size();

	Companion<T> companion();

	Collection<T> values();


	long getAndIncreaseInstanceId();

	long getNextInstanceId();

	T internalRemoveInplace(Long instanceId);

	T internalUpdateInplace(AbstractPersistentObject newInstance);

	T internalCreateInplace(AbstractPersistentObject newInstance);

	
}
