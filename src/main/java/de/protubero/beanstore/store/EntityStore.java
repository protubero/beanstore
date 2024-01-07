package de.protubero.beanstore.store;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;

public interface EntityStore<T extends AbstractPersistentObject> {

	boolean isImmutable();
	
	T get(Long id);

	T getNullable(Long id);
	
	Optional<T> getOptional(Long id);

	Stream<T> objects();
	
	int size();

	Companion<T> companion();

	Collection<T> values();


	long getAndIncreaseInstanceId();

	long getNextInstanceId();

	/**
	 * Return the removed instane
	 * 
	 * @param instanceId
	 * @return 
	 */
	T internalRemoveInplace(Long instanceId);

	/**
	 * The method has an unusual semantic in order to support the StoreWriter logic;
	 * 
	 * Return the updated instance. If there is no existing version to be updated, return null and 
	 * do not change anything!
	 * 
	 * @param newInstance
	 * @return
	 */
	T internalUpdateInplace(AbstractPersistentObject newInstance);

	/**
	 * The method has an unusual semantic in order to support the StoreWriter logic;
	 * 
	 * Return null if there is no prior version of the instance in the store. If there is a prior version,
	 * return this and do not change anything.
	 * 
	 * @param newInstance
	 * @return
	 */
	T internalCreateInplace(AbstractPersistentObject newInstance);

	boolean isEmpty();

	
}
