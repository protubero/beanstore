package de.protubero.beanstore.builder.blocks;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.CompanionRegistry;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.MutableEntityStoreSet;

public class DynamicCompanionSet  implements CompanionSet {

	private MutableEntityStoreSet entityStoreSet;
	
	public DynamicCompanionSet(MutableEntityStoreSet aEntityStoreSet) {
		entityStoreSet = aEntityStoreSet;
	}

	@Override
	public <T extends AbstractPersistentObject> Optional<Companion<T>> companionByClass(Class<T> entityClazz) {
		throw new AssertionError();
	}

	@Override
	public Optional<Companion<? extends AbstractPersistentObject>> companionByAlias(String alias) {
		Optional<Companion<? extends AbstractPersistentObject>> result = entityStoreSet.companionsShip().companionByAlias(alias);
		if (result.isEmpty()) {
			entityStoreSet.register(CompanionRegistry.getOrCreateMapCompanion(alias));
			result = entityStoreSet.companionsShip().companionByAlias(alias);
			if (result.isEmpty()) {
				throw new AssertionError();
			}
		}	
		return result;
	}

	@Override
	public Iterator<Companion<?>> iterator() {
		return entityStoreSet.companionsShip().iterator();
	}

	@Override
	public Stream<Companion<?>> companions() {
		return entityStoreSet.companionsShip().companions();
	}

	@Override
	public boolean isEmpty() {
		return entityStoreSet.companionsShip().isEmpty();
	}



}
