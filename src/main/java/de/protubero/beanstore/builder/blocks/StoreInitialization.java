package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.CompanionSetImpl;

public class StoreInitialization {

	private boolean autoCreateEntities; 
	private List<Migration> migrations = new ArrayList<>();
	private Consumer<BeanStoreTransaction> initMigration;
	private CompanionSetImpl companionSet = new CompanionSetImpl();
	
	/**
	 * Register a Java Bean class. It must be a descendant of AbstractEntity.
	 * 
	 */
	public <X extends AbstractEntity> BeanStoreEntity<X> registerEntity(Class<X> beanClass) {
		return companionSet.add(beanClass);
	}

	public MapObjectCompanion registerMapEntity(String alias) {
		return companionSet.addMapEntity(alias);
	}

	void register(Companion<?> companion) {
		companionSet.add(companion);
	}
	
	
	public void addMigration(Migration migration) {

		if (migration.getMigrationId().startsWith("_")) {
			throw new RuntimeException("migration id must not start with an underscore character: " + migration.getMigrationId());
		}

		// ignore case is intentional
		if (migrations.stream().filter(m -> m.getMigrationId().equalsIgnoreCase(migration.getMigrationId())).findAny().isPresent()) {
			throw new RuntimeException("duplicate migration id: " + migration.getMigrationId());
		}
		
		migrations.add(migration);
	}

	public void initNewStore(Consumer<BeanStoreTransaction> migration) {
		if (initMigration != null) {
			throw new RuntimeException("duplicate init migration");
		} else {
			initMigration = migration;
		}
	}

	public List<Migration> getMigrations() {
		return Collections.unmodifiableList(migrations);
	}

	public Consumer<BeanStoreTransaction> getInitMigration() {
		return initMigration;
	}

	public CompanionSet getCompanionSet() {
		return companionSet;
	}
	
	public String initMigrationId() {
		if (migrations.size() == 0) {
			return null;
		} else {
			return migrations.get(migrations.size() - 1).getMigrationId();
		}
	}


	public boolean isAutoCreateEntities() {
		return autoCreateEntities;
	}

	public void setAutoCreateEntities(boolean autoCreateEntities) {
		this.autoCreateEntities = autoCreateEntities;
	}

	public void check() {
		if (!autoCreateEntities && companionSet.isEmpty()) {
			throw new RuntimeException("Zero registered entities");
		}
	}


			
}
