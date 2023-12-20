package de.protubero.beanstore.store;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.Companion;

public interface EntityStoreSet<E extends EntityStore<?>> extends Iterable<E> {
		
	boolean isImmutable();
	
	
	<T extends AbstractPersistentObject> EntityStore<T> store(String alias);
	
	<T extends AbstractPersistentObject> EntityStore<T> store(Class<T> aClass);

	@SuppressWarnings("unchecked")
	default <T extends AbstractPersistentObject> EntityStore<T> store(T ref) {
		return (EntityStore<T>) store(ref.companion().entityClass());
	}
	
	<T extends AbstractPersistentObject> Optional<EntityStore<T>> storeOptional(String alias);

	<T extends AbstractPersistentObject> Optional<EntityStore<T>> storeOptional(Class<T> aClass);


	EntityStoreSet<E> internalCloneStoreSet();

	public default CompanionShip companionsShip() {
		return new CompanionShip() {
			
			@Override
			public Iterator<Companion<?>> iterator() {
				final Iterator<E> storeIterator = EntityStoreSet.this.iterator();
				return new Iterator<Companion<?>>() {

					@Override
					public boolean hasNext() {
						return storeIterator.hasNext();
					}

					@Override
					public Companion<?> next() {
						return storeIterator.next().companion();
					}
					
				};
			}
			
			@Override
			public boolean isEmpty() {
				return hasNoEntityStores();
			}
			
			@Override
			public Stream<Companion<?>> companions() {
				return StreamSupport.stream(EntityStoreSet.this.spliterator(), false).map(store -> store.companion());
			}
			
			@Override
			public <T extends AbstractPersistentObject> Optional<Companion<T>> companionByClass(Class<T> entityClazz) {
				return storeOptional(entityClazz).map(store -> (Companion<T>) store.companion());
			}
			
			@Override
			public <T extends AbstractPersistentObject> Optional<Companion<T>> companionByAlias(String alias) {
				return storeOptional(alias).map(store -> (Companion<T>) store.companion());
			}
		};
	}


	boolean hasNoData();


	boolean hasNoEntityStores();
	
	
}
