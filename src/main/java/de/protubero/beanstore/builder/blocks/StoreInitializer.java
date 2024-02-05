package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.builder.MigrationTransactionImpl;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.EntityCompanion;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.impl.BeanStoreSnapshotImpl;
import de.protubero.beanstore.impl.BeanStoreTransactionImpl;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.store.ImmutableEntityStoreBase;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;
import de.protubero.beanstore.tx.Transaction;

public class StoreInitializer implements Consumer<InterimStore> {

	public static final Logger log = LoggerFactory.getLogger(StoreInitializer.class);

	public static final String INIT_ID = "_INIT_";
	
	
	private StoreInitialization initialization;

	private StoreInitializer(StoreInitialization initialization) {
		this.initialization = Objects.requireNonNull(initialization);
	}

	public static StoreInitializer of(StoreInitialization initialization) {
		return new StoreInitializer(initialization);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void accept(InterimStore interimStore) {
		if (interimStore.getStore() == null) {
			log.info("Init store");
			interimStore.setStore(new ImmutableEntityStoreSet(initialization.getCompanionSet(), 0));
			
			// we need to store the transaction even if it is empty
			var tx = Transaction.of(initialization.getCompanionSet(), initialization.initMigrationId(), PersistentTransaction.TRANSACTION_TYPE_MIGRATION);
			if (initialization.getInitMigration() != null) {
				initialization.getInitMigration().accept(new BeanStoreTransactionImpl(tx));
			}	
			interimStore.execute(tx);

			if (tx.failed()) {
				throw new RuntimeException("Init store failed", tx.failure());
			}
			if (interimStore.getStore().version() != 1) {
				throw new AssertionError("Initialized Store has version no " + interimStore.getStore().version());
			}
		} else {
			log.info("Migrate store");
			
			// enhance registered entities with persisted entities not yet registered 
			if (initialization.isAutoCreateEntities()) {
				interimStore.getStore().companionsShip().forEach(companion -> {
					if (initialization.getCompanionSet().companionByAlias(companion.alias()).isEmpty()) {
						initialization.register(companion);
					}
				});
			}
			
			// fill up map store with registered entities without any persisted instances
			initialization.getCompanionSet().companions().forEach(companion -> {
				if (interimStore.getStore().companionsShip().companionByAlias(companion.alias()).isEmpty()) {
					((MutableEntityStoreSet) interimStore.getStore()).register(new MapObjectCompanion(companion.alias()));
				}
			});

			
			List<String> appliedMigrationIds = interimStore.appliedMigrationIds();
			
			// migrate store		
			if (appliedMigrationIds.size() == 0) {
				throw new AssertionError("missing init migration");
			} else {
				log.info("No. of applied migration transactions (incl. init): " + appliedMigrationIds.size());
			}
			
			List<Migration> migrationsToApply = initialization.findMigrationsToApply(appliedMigrationIds);
			
			// apply remaining migrations
			for (Migration migration : migrationsToApply) {

				var tx = Transaction.of(interimStore.companionSet(), migration.getMigrationId(),
						PersistentTransaction.TRANSACTION_TYPE_MIGRATION);
				migration.getMigration().accept(new MigrationTransactionImpl(tx, new BeanStoreSnapshotImpl(interimStore.getStore())));

				interimStore.execute(tx);
				
				if (tx.failed()) {
					throw new RuntimeException("Migration failed: " + migration.getMigrationId(), tx.failure());
				}
								
				log.info("migration applied: " + migration.getMigrationId() + " (" + tx.getInstanceEvents().size() + ")");
			}


			// convert to immutable store set
			List<ImmutableEntityStoreBase<?>> entityStoreBaseList = new ArrayList<>();
			
			// 1. iterate over loaded entities
			MutableEntityStoreSet mapStore = (MutableEntityStoreSet) interimStore.getStore();
			
			for (MutableEntityStore<?> es : mapStore) {
				ImmutableEntityStoreBase<AbstractPersistentObject> newEntityStore = new ImmutableEntityStoreBase<>();
				entityStoreBaseList.add(newEntityStore);
				
				String entityAlias = es.companion().alias();	
				Optional<Companion<? extends AbstractPersistentObject>> registeredEntityCompanionOpt = initialization.getCompanionSet().companionByAlias(entityAlias);
				if (registeredEntityCompanionOpt.isEmpty()) {
					if (es.isEmpty()) {
						log.info("Ignoring deleted entity " + entityAlias);
					} else {
						throw new RuntimeException("No registered entity matching loaded data: " + entityAlias);
					}	
				} else {
					final Companion<? extends AbstractPersistentObject> registeredEntityCompanion = registeredEntityCompanionOpt.get();
					newEntityStore.setNextInstanceId(es.getNextInstanceId());
					newEntityStore.setCompanion((Companion<AbstractPersistentObject>) registeredEntityCompanion);
					if (registeredEntityCompanion.isMapCompanion()) {
						newEntityStore.setObjectMap((Map<Long, AbstractPersistentObject>) es.getObjectMap());
					} else {
						// convert maps to entities 
						Map<Long, AbstractPersistentObject> initialEntityMap = new HashMap<>();
						es.objects().forEach(mapObj -> {
							AbstractEntity newInstance = (AbstractEntity) registeredEntityCompanion.createInstance();
							newInstance.id(mapObj.id());
							newInstance.state(State.PREPARE);
							// copy all properties
							((EntityCompanion<AbstractEntity>) registeredEntityCompanion).transferProperties((Map<String, Object>) mapObj, newInstance);

							newInstance.state(State.STORED);
							initialEntityMap.put(newInstance.id(), newInstance);
						});
						newEntityStore.setObjectMap(initialEntityMap);
					}
				}
			}	

			
			// Create final store set
			ImmutableEntityStoreSet finalStoreSet = new ImmutableEntityStoreSet(
					entityStoreBaseList.toArray(new ImmutableEntityStoreBase[entityStoreBaseList.size()]), mapStore.version());
			
			interimStore.setStore(finalStoreSet);
		}
		
	}	
	
	
}
