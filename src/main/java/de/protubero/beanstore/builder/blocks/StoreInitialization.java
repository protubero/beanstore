package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.CompanionSetImpl;

public class StoreInitialization {

	private List<Migration> migrations = new ArrayList<>();
	private Consumer<BeanStoreTransaction> initMigration = (tx) -> {};
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
			return StoreInitializer.INIT_ID;
		} else {
			return StoreInitializer.INIT_ID + (migrations.get(migrations.size() - 1)).getMigrationId();
		}
	}

	/**
	 * It is not checked whether the sequence of migrations of the persisted data matches the registered
	 * migrations! Its enough to find a registered migration matching the last migration applied to the data.
	 * 
	 * @param appliedMigrationIds
	 * @return
	 */
	public List<Migration> findMigrationsToApply(List<String> appliedMigrationIds) {
		String lastMigrationId = appliedMigrationIds.get(appliedMigrationIds.size() -1);
		if (lastMigrationId == null) {
			// store was initialized with no migration history presen
			return migrations;
		} else {
			Optional<Migration> lastMigrationApplied = migrations.stream().filter(m -> m.getMigrationId().equals(lastMigrationId)).findAny();
			if (lastMigrationApplied.isEmpty()) {
				throw new RuntimeException("missing migration id " + lastMigrationId);
			} else {
				// apply remaining migrations
				return migrations.subList(migrations.indexOf(lastMigrationApplied.get()) + 1, migrations.size());
			}
		}
	}

			
}