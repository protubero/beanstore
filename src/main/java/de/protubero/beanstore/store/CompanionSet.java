package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.Companion;
import de.protubero.beanstore.base.entity.EntityCompanion;
import de.protubero.beanstore.base.entity.MapObjectCompanion;

public class CompanionSet implements CompanionShip {

	private List<Companion<?>> companionList = new ArrayList<>();
	

	public void addMapEntity(String alias) {
		if (companionByAlias(alias) != null) {
			throw new RuntimeException("Companion with alias already exists: " + alias);
		}
		companionList.add(new MapObjectCompanion(alias));		
	}


	public <T extends AbstractEntity> EntityCompanion<T> add(Class<T> entityClazz) {
		Optional<Companion<T>> companion = companionByClass(entityClazz);
		if (companion.isPresent()) {
			throw new RuntimeException("Duplicate alias: " + companion.get().alias() + " [" + entityClazz + "]");
		}
		EntityCompanion<T> newCompanion = new EntityCompanion<T>(entityClazz);
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
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractPersistentObject> Optional<Companion<T>> companionByAlias(String alias) {
		for (Companion<?> companion : companionList) {
			if (companion.alias().equals(alias)) {
				return Optional.of((Companion<T>) companion);
			}
		}
		return Optional.empty();
	}
	


	@Override
	public Stream<Companion<?>> companions() {
		return companionList.stream();
	}



	
}
