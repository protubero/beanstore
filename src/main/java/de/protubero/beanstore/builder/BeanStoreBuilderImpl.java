package de.protubero.beanstore.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.entity.BeanStoreException;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.EntityCompanion;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.impl.BeanStoreImpl;
import de.protubero.beanstore.impl.BeanStoreSnapshotImpl;
import de.protubero.beanstore.impl.BeanStoreTransactionImpl;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;
import de.protubero.beanstore.pluginapi.BuilderTransactionListener;
import de.protubero.beanstore.pluginapi.PersistenceReadListener;
import de.protubero.beanstore.pluginapi.PersistenceWriteListener;
import de.protubero.beanstore.store.CompanionSetImpl;
import de.protubero.beanstore.store.ImmutableEntityStoreBase;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;
import de.protubero.beanstore.tx.StoreWriter;
import de.protubero.beanstore.tx.Transaction;

public class BeanStoreBuilderImpl extends AbstractStoreBuilder implements BeanStoreBuilder {

	public static final String INIT_ID = "_INIT_";

	public static final Logger log = LoggerFactory.getLogger(BeanStoreBuilderImpl.class);

	private List<Migration> migrations = new ArrayList<>();
	private Consumer<BeanStoreTransaction> initMigration;

	private CompanionSetImpl companionSet = new CompanionSetImpl();


	private List<BeanStorePlugin> plugins = new ArrayList<>();
	private List<BuilderTransactionListener> transactionListener = new ArrayList<>();
	private List<PersistenceReadListener> persistenceReadListener = new ArrayList<>();
	private List<PersistenceWriteListener> persistenceWriteListener = new ArrayList<>();

	private List<AppliedMigration> appliedMigrations = new ArrayList<>();

	
	BeanStoreBuilderImpl(TransactionPersistence persistence) {
		super(persistence);
	}

	
	@Override
	public void addPlugin(BeanStorePlugin aPlugin) {
		throwExceptionIfAlreadyCreated();
		
		for (BeanStorePlugin plugin : plugins) {
			if (aPlugin == plugin) {
				throw new RuntimeException("Duplicate plugin registration");
			}
		}
		
		plugins.add(aPlugin);
		if (aPlugin instanceof BuilderTransactionListener) {
			transactionListener.add((BuilderTransactionListener) aPlugin);
		}
		if (aPlugin instanceof PersistenceReadListener) {
			persistenceReadListener.add((PersistenceReadListener) aPlugin);
		}
		if (aPlugin instanceof PersistenceWriteListener) {
			persistenceWriteListener.add((PersistenceWriteListener) aPlugin);
		}
	}

	/**
	 * Register a Java Bean class. It must be a descendant of AbstractEntity.
	 * 
	 */
	@Override
	public <X extends AbstractEntity> BeanStoreEntity<X> registerEntity(Class<X> beanClass) {
		throwExceptionIfAlreadyCreated();
		return companionSet.add(beanClass);
	}

	@Override
	public MapObjectCompanion registerMapEntity(String alias) {
		throwExceptionIfAlreadyCreated();

		return companionSet.addMapEntity(alias);
	}
	
	@Override
	public void addMigration(String migrationId, Consumer<MigrationTransaction> migration) {
		throwExceptionIfAlreadyCreated();
		if (!migrationId.trim().equals(migrationId)) {
			throw new RuntimeException("invalid migration id");
		}

		if (migrationId.startsWith("_")) {
			throw new RuntimeException("migration id must not start with an underscore character: " + migrationId);
		}

		// ignore case is intentional
		if (migrations.stream().filter(m -> m.getMigrationId().equalsIgnoreCase(migrationId)).findAny().isPresent()) {
			throw new RuntimeException("duplicate migration id: " + migrationId);
		}
		migrations.add(new Migration(migrationId, migration));
	}

	@Override
	public void initNewStore(Consumer<BeanStoreTransaction> migration) {
		throwExceptionIfAlreadyCreated();
		if (initMigration != null) {
			throw new RuntimeException("duplicate init migration");
		} else {
			initMigration = migration;
		}
	}


	private ImmutableEntityStoreSet initStore(ImmutableEntityStoreSet aStoreSet) {
		log.info("Init store");

		// init store
		Consumer<BeanStoreTransaction> initialMigration = initMigration;
		if (initialMigration == null) {
			// default migration: do nothing
			initialMigration = (bst) -> {
			};
		}

		String initialTransactionId = INIT_ID;
		if (migrations.size() > 0) {
			String lastMigrationId = migrations.get(migrations.size() - 1).getMigrationId();
			initialTransactionId += lastMigrationId;
		}

		// always remember last migration id
		var tx = Transaction.of(companionSet, initialTransactionId, PersistentTransaction.TRANSACTION_TYPE_MIGRATION);
		initialMigration.accept(new BeanStoreTransactionImpl(tx));
		aStoreSet = createStoreWriter().execute(tx, aStoreSet);
		
		if (tx.failed()) {
			throw new RuntimeException("Init store failed", tx.failure());
		}

		// call transaction listener
		for (BuilderTransactionListener listener : transactionListener) {
			listener.onInitTransaction(tx);
		}
		
		return aStoreSet;
	}





	private void migrate(final MutableEntityStoreSet mapStore) {
		// fill up map store with registered entities without any persisted instances
		companionSet.companions().forEach(companion -> {
			if (mapStore.companionsShip().companionByAlias(companion.alias()).isEmpty()) {
				mapStore.register(new MapObjectCompanion(companion.alias()));
			}
		});
		
		// migrate store
		String databaseState = null;
		if (appliedMigrations.size() == 0) {
			throw new AssertionError("missing init migration");
		} else {
			log.info("No. of applied migration transactions: " + appliedMigrations.size());
		}

		// find database state (i.e. last applied migration)
		databaseState = appliedMigrations.get(appliedMigrations.size() - 1).getMigrationId();

		if (databaseState.startsWith(INIT_ID)) {
			if (appliedMigrations.size() != 1) {
				throw new AssertionError("unexpected");
			}
			if (databaseState.length() != INIT_ID.length()) {
				databaseState = databaseState.substring(INIT_ID.length());
				log.info("loaded db state: " + databaseState);
			} else {
				log.info("loaded db state: _initialised_");
				databaseState = null;
			}
		}

		int migrationStartIdx = 0;
		if (databaseState != null) {
			// find migration which is referred to by the database state
			var tempIdx = 0;
			var lastMigrationIdx = -1;
			for (var mig : migrations) {
				if (mig.getMigrationId().equals(databaseState)) {
					lastMigrationIdx = tempIdx;
					break;
				}
				tempIdx++;
			}
			if (lastMigrationIdx == -1) {
				throw new RuntimeException("missing migration id " + databaseState);
			} else {
				migrationStartIdx = lastMigrationIdx + 1;
			}
		}

		
		StoreWriter migrationStoreWriter = createStoreWriter();
		MutableEntityStoreSet migratedMapStore = mapStore; 
		// apply remaining migrations
		if (migrationStartIdx < migrations.size()) {
			for (int idx = migrationStartIdx; idx < migrations.size(); idx++) {
				var mig = migrations.get(idx);

				var tx = Transaction.of(migratedMapStore.companionsShip(), mig.getMigrationId(),
						PersistentTransaction.TRANSACTION_TYPE_MIGRATION);
				mig.getMigration().accept(new MigrationTransactionImpl(tx, new BeanStoreSnapshotImpl(migratedMapStore)));
				migratedMapStore = migrationStoreWriter.execute(tx, migratedMapStore);
				
				if (tx.failed()) {
					throw new RuntimeException("Migration failed: " + mig.getMigrationId(), tx.failure());
				}
				
				// call transaction listener
				for (BuilderTransactionListener listener : transactionListener) {
					listener.onMigrationTransaction(tx);
				}
				
				log.info("migration applied: " + mig.getMigrationId() + " (" + tx.getInstanceEvents().size() + ")");
			}
		}
		
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public BeanStore build() {
		
		if (companionSet.isEmpty()) {
			throw new RuntimeException("Mode RegisteredEntities does not allow zero registered entities");
		}
		
		startBuildProcess();
		
		plugins.forEach(plugin -> plugin.onStartCreate(this));

		MutableEntityStoreSet mapStore = null;
		ImmutableEntityStoreSet finalStoreSet = null;
		
		mapStore = loadMapStore();
		
		if (mapStore != null) {
			migrate(mapStore);
		}
	
		if (mapStore == null) {
			// i.e. either no file set or file does not exist
			finalStoreSet = new ImmutableEntityStoreSet(companionSet, 0);
			finalStoreSet = initStore(finalStoreSet);
			if (finalStoreSet.version() != 1) {
				throw new AssertionError("Initialized Store has version no " + finalStoreSet.version());
			}
		} else {
			List<ImmutableEntityStoreBase<?>> entityStoreBaseList = new ArrayList<>();
			
			// 1. iterate over loaded entities
			for (MutableEntityStore<?> es : mapStore) {
				ImmutableEntityStoreBase<AbstractPersistentObject> newEntityStore = new ImmutableEntityStoreBase<>();
				entityStoreBaseList.add(newEntityStore);
				
				String entityAlias = es.companion().alias();	
				Optional<Companion<? extends AbstractPersistentObject>> registeredEntityCompanionOpt = companionSet.companionByAlias(entityAlias);
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

			
			/**
			 * Add registered entities when there were no persisted data of these entities
			 */
			// 1. iterate over registered entities
			for (Companion<?> companion : companionSet) {
				// if alias not in list yet ...
				if (entityStoreBaseList.stream().filter(esb -> esb.getCompanion().alias().equals(companion.alias())).findAny().isEmpty()) {
					// .. add it
					ImmutableEntityStoreBase<AbstractPersistentObject> newEntityStore = new ImmutableEntityStoreBase<>();
					newEntityStore.setCompanion((Companion<AbstractPersistentObject>) companion);
					entityStoreBaseList.add(newEntityStore);
				}
			}

			
			// Create final store set
			finalStoreSet = 
					new ImmutableEntityStoreSet(
							entityStoreBaseList.toArray(new ImmutableEntityStoreBase[entityStoreBaseList.size()]), mapStore.version());
		}

		
		BeanStoreImpl beanStoreImpl = endBuildProcess(finalStoreSet);

		plugins.forEach(plugin -> {
			plugin.onEndCreate(beanStoreImpl);
		});
		
		return beanStoreImpl;
	}



	@Override
	protected void onReadMigrationTransaction(PersistentTransaction pt) {
		appliedMigrations.add(new AppliedMigration(Objects.requireNonNull(pt.getTransactionId())));
	}


	@Override
	protected void onReadTransaction(PersistentTransaction pt) {
		for (PersistenceReadListener listener : persistenceReadListener) {
			listener.onReadTransaction(pt);
		}
	}


	@Override
	protected void onWriteTransaction(PersistentTransaction pt) {
		for (PersistenceWriteListener listener : persistenceWriteListener) {
			listener.onWriteTransaction(pt);
		}
	}





}
