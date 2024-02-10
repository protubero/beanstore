package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.EntityCompanion;
import de.protubero.beanstore.entity.MapObjectCompanion;

public class CompanionSetImpl implements CompanionSet {

	private List<Companion<?>> companionList = new ArrayList<>();
	

	public void add(Companion<?> companion) {
		companionList.add(companion);
	}
	
	public MapObjectCompanion addMapEntity(String alias) {
		if (companionByAlias(alias).isPresent()) {
			throw new RuntimeException("Companion with alias already exists: " + alias);
		}
		MapObjectCompanion comp = MapObjectCompanion.getOrCreate(alias);
		companionList.add(comp);		
		return comp;
	}


	public <T extends AbstractEntity> EntityCompanion<T> add(Class<T> entityClazz) {
		Optional<Companion<T>> companion = companionByClass(entityClazz);
		if (companion.isPresent()) {
			throw new RuntimeException("Duplicate alias: " + companion.get().alias() + " [" + entityClazz + "]");
		}
		EntityCompanion<T> newCompanion = EntityCompanion.getOrCreate(entityClazz);
		companionList.add(newCompanion);
		return newCompanion;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> Optional<Companion<T>> companionByClass(Class<T> entityClazz) {
		for (Companion<?> companion : companionList) {
			if (companion.entityClass() == entityClazz) {
				return Optional.of((Companion<T>) companion);
			}
		}
		return Optional.empty();
	}
	
	public Optional<Companion<? extends AbstractPersistentObject>> companionByAlias(String alias) {
		for (Companion<?> companion : companionList) {
			if (companion.alias().equals(alias)) {
				return Optional.of(companion);
			}
		}
		return Optional.empty();
	}
	


	@Override
	public Stream<Companion<?>> companions() {
		return companionList.stream();
	}


	@Override
	public boolean isEmpty() {
		return companionList.isEmpty();
	}


	@Override
	public Iterator<Companion<?>> iterator() {
		return companionList.iterator();
	}





	
}
