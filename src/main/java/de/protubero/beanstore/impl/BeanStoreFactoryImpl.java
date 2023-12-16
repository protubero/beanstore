package de.protubero.beanstore.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Registration;
import com.esotericsoftware.kryo.kryo5.Serializer;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreFactory;
import de.protubero.beanstore.api.BeanStorePlugin;
import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.api.MigrationTransaction;
import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.Transition;
import de.protubero.beanstore.base.entity.BeanStoreEntity;
import de.protubero.beanstore.base.entity.Companion;
import de.protubero.beanstore.base.entity.EntityCompanion;
import de.protubero.beanstore.base.entity.MapObjectCompanion;
import de.protubero.beanstore.base.tx.TransactionPhase;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.persistence.base.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.base.PersistentPropertyUpdate;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.persistence.impl.DeferredTransactionWriter;
import de.protubero.beanstore.persistence.impl.KryoConfiguration;
import de.protubero.beanstore.persistence.impl.KryoPersistence;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.ImmutableEntityStoreBase;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;

public class BeanStoreFactoryImpl implements BeanStoreFactory {

	public static final String INIT_ID = "_INIT_";

	public static final Logger log = LoggerFactory.getLogger(BeanStoreFactory.class);

	private File file;
	private boolean acceptUnregisteredEntities = false;
	private List<Migration> migrations = new ArrayList<>();
	private Consumer<BeanStoreTransaction> initMigration;

	private CompanionSet companionSet = new CompanionSet();

	private boolean created;

	private List<BeanStorePlugin> plugins = new ArrayList<>();

	// Fields are used at build time
	private KryoConfiguration kryoConfig = new KryoConfiguration();
	private KryoPersistence persistence;
	private DeferredTransactionWriter deferredTransactionWriter;
	private List<AppliedMigration> appliedMigrations = new ArrayList<>();

	public BeanStoreFactoryImpl(File file) {
		this.file = Objects.requireNonNull(file);
		init();
	}

	private void init() {
	}

	public BeanStoreFactoryImpl() {
		init();
	}

	@Override
	public void addPlugin(BeanStorePlugin plugin) {
		throwExceptionIfAlreadyCreated();
		plugins.add(plugin);
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

	private void throwExceptionIfAlreadyCreated() {
		// It can only be created once
		if (created) {
			throw new RuntimeException("bean store has already been created");
		}

	}

	private void initStore(ImmutableEntityStoreSet aStoreSet) {
		log.info("Init store");

		// init store
		Consumer<BeanStoreTransaction> initialMigration = initMigration;
		initialMigration = initMigration;
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

		plugins.forEach(plugin -> plugin.onInitTransaction(tx));
	}

	private MutableEntityStoreSet loadMapStore() {
		plugins.forEach(plugin -> plugin.onOpenFile(file));

		// load transactions
		persistence = new KryoPersistence(kryoConfig, file);
		deferredTransactionWriter = new DeferredTransactionWriter(persistence.writer());

		boolean noStoredTransactions = persistence.isEmpty();
		if (noStoredTransactions) {
			return null;
		} else {
			MutableEntityStoreSet mapStore = new MutableEntityStoreSet();
			mapStore.setAcceptNonGeneratedIds(true);
			
			load(mapStore, persistence.reader(), appliedMigrations);

			// handle loaded entity stores which has not been registered
			for (MutableEntityStore<?> es : mapStore) {
				String tAlias = es.companion().alias();
				Optional<Companion<AbstractPersistentObject>> registeredCompanion = companionSet
						.companionByAlias(tAlias);
				if (!registeredCompanion.isPresent()) {
					if (acceptUnregisteredEntities) {
						companionSet.addMapEntity(tAlias);
					} else {
						throw new RuntimeException("Found un-registered entity in file: " + tAlias);
					}
				}
			}
			return mapStore;
		}

	}

	private StoreWriter createStoreWriter() {
		StoreWriter storeWriter = new StoreWriter();

		storeWriter.registerSyncInternalTransactionListener(TransactionPhase.PERSIST, t -> {
			plugins.forEach(plugin -> plugin.onWriteTransaction(t.persistentTransaction));
			if (deferredTransactionWriter != null) {
				deferredTransactionWriter.append(t.persistentTransaction);
			}
		});
		
		return storeWriter;
	}

	private void migrate(MutableEntityStoreSet mapStore) {
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
		// apply remaining migrations
		if (migrationStartIdx < migrations.size()) {
			for (int idx = migrationStartIdx; idx < migrations.size(); idx++) {
				var mig = migrations.get(idx);

				var tx = Transaction.of(mapStore, mig.getMigrationId(),
						PersistentTransaction.TRANSACTION_TYPE_MIGRATION);
				mig.getMigration().accept(new MigrationTransactionImpl(tx, new BeanStoreStateImpl(mapStore)));
				mapStore = migrationStoreWriter.execute(tx, mapStore);
				plugins.forEach(plugin -> plugin.onMigrationTransaction(tx));

				log.info("migration applied: " + mig.getMigrationId() + " (" + tx.getInstanceEvents().size() + ")");
			}
		}

	}

	@Override
	public BeanStore create() {
		throwExceptionIfAlreadyCreated();
		created = true;

		plugins.forEach(plugin -> plugin.onStartCreate(this));


		MutableEntityStoreSet mapStore = null;
		boolean initStore = true;
		boolean migrateMapStore = true;
		if (file != null) {
			mapStore = loadMapStore();
			if (mapStore == null) {
				migrateMapStore = false;
				initStore = true;
			} else {
				migrateMapStore = true;
				initStore = false;
			}
		} else {
			migrateMapStore = false;
			initStore = true;
		}

		if (migrateMapStore) {
			migrate(mapStore);
		}
		if (initStore) {
			mapStore = new MutableEntityStoreSet(companionSet);
		}

		List<ImmutableEntityStoreBase<?>> entityStoreBaseList = convertMapStoreToFinalStore(mapStore);
		
		// Create final store set
		ImmutableEntityStoreSet finalStoreSet = 
				new ImmutableEntityStoreSet(
						entityStoreBaseList.toArray(new ImmutableEntityStoreBase[entityStoreBaseList.size()]));

		if (initStore) {
			initStore(finalStoreSet);
		}
		
		if (deferredTransactionWriter != null) {
			// persist migration transactions
			// this is the first time that data gets written to the file
			deferredTransactionWriter.switchToNonDeferred();
		}	
		

		Runnable onCloseStoreAction = () -> {try {
			if (deferredTransactionWriter != null) {
				log.info("Closing transaction writer");
				deferredTransactionWriter.close();
			}	
		} catch (Exception e) {
			log.error("Error closing transaction writer", e);
		}};			
		
		BeanStoreImpl beanStoreImpl = new BeanStoreImpl(finalStoreSet, onCloseStoreAction, createStoreWriter());

		BeanStoreStateImpl readAccess = new BeanStoreStateImpl(finalStoreSet);
		plugins.forEach(plugin -> {
			plugin.onEndCreate(beanStoreImpl, readAccess);
		});
		
		return beanStoreImpl;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<ImmutableEntityStoreBase<?>> convertMapStoreToFinalStore(MutableEntityStoreSet mapStore) {
		List<ImmutableEntityStoreBase<?>> entityStoreBaseList = new ArrayList<>();
		
		// convert maps to beans or create them new
		for (MutableEntityStore<?> es : mapStore) {
			ImmutableEntityStoreBase<AbstractPersistentObject> newEntityStore = new ImmutableEntityStoreBase<>();
			entityStoreBaseList.add(newEntityStore);
			
			String entityAlias = es.companion().alias();			
			Companion<? extends AbstractPersistentObject> registeredEntityCompanion = companionSet.companionByAlias(entityAlias)
					.get();

			newEntityStore.setNextInstanceId(es.getNextInstanceId());
			newEntityStore.setCompanion((Companion) registeredEntityCompanion);

			if (registeredEntityCompanion.isMapCompanion()) {
				newEntityStore.setObjectMap((Map) es.getObjectMap());
			} else {
				Map<Long, AbstractPersistentObject> initialEntityMap = new HashMap<>();
				es.objects().forEach(mapObj -> {
					AbstractEntity newInstance = (AbstractEntity) registeredEntityCompanion.createInstance();
					newInstance.id(mapObj.id());
					// copy all properties
					((EntityCompanion<AbstractEntity>) registeredEntityCompanion).transferProperties((Map<String, Object>) mapObj, newInstance);
					

					// set tags ref to entity
					/*
					 * if (isTaggedEntity) { ((AbstractTaggedEntity)
					 * newInstance).getTags().setEntity((AbstractTaggedEntity) newInstance); }
					 */

					newInstance.applyTransition(Transition.INSTANTIATED_TO_READY);
					plugins.forEach(plugin -> plugin.validate(newInstance));
					initialEntityMap.put(newInstance.id(), newInstance);
				});
				newEntityStore.setObjectMap(initialEntityMap);
			}
		}
		
		return entityStoreBaseList;
	}

	@SuppressWarnings("unchecked")
	private void load(MutableEntityStoreSet store, TransactionReader reader, List<AppliedMigration> appliedMigrations) {
		reader.load(pt -> {
			plugins.forEach(plugin -> plugin.onReadTransaction(pt));

			// log list of migration transactions
			if (pt.getTransactionType() == PersistentTransaction.TRANSACTION_TYPE_MIGRATION) {
				appliedMigrations.add(new AppliedMigration(Objects.requireNonNull(pt.getTransactionId())));
			}

			PersistentInstanceTransaction[] instanceTransactions = pt.getInstanceTransactions();
			if (instanceTransactions == null) {
				if (pt.getTransactionId() == null) {
					throw new RuntimeException("empty transaction");
				} else {
					instanceTransactions = new PersistentInstanceTransaction[0];
				}
			}

			for (PersistentInstanceTransaction pit : instanceTransactions) {
				@SuppressWarnings({ "rawtypes" })
				MutableEntityStore entityStore = (MutableEntityStore) store.storeOptional(pit.getAlias())
						.orElseGet(() -> {
							return (MutableEntityStore) store.register(new MapObjectCompanion(pit.getAlias()));
						});

				AbstractPersistentObject instance = null;
				switch (pit.getType()) {
				case PersistentInstanceTransaction.TYPE_CREATE:
					instance = entityStore.companion().createInstance();
					instance.id(pit.getId());
					break;
				case PersistentInstanceTransaction.TYPE_DELETE:
					if (entityStore.remove(pit.getId()) == null) {
						throw new AssertionError();
					}
					break;
				case PersistentInstanceTransaction.TYPE_UPDATE:
					instance = entityStore.get(pit.getId());
					if (instance == null) {
						throw new AssertionError();
					}
					instance.applyTransition(Transition.READY_TO_INPLACEUPDATE);
					break;
				default:
					throw new AssertionError();
				}

				// update fields
				switch (pit.getType()) {
				case PersistentInstanceTransaction.TYPE_CREATE:
				case PersistentInstanceTransaction.TYPE_UPDATE:
					if (pit.getPropertyUpdates() != null) {
						for (PersistentPropertyUpdate propertyUpdate : pit.getPropertyUpdates()) {
							instance.put(propertyUpdate.getProperty(), propertyUpdate.getValue());
						}
					}
				}

				switch (pit.getType()) {
				case PersistentInstanceTransaction.TYPE_CREATE:
					instance.applyTransition(Transition.INSTANTIATED_TO_READY);
					entityStore.put(instance);
					break;
				case PersistentInstanceTransaction.TYPE_UPDATE:
					instance.applyTransition(Transition.INPLACEUPDATE_TO_READY);
					break;
				}

			}
		});
	}

	@Override
	public boolean isAcceptUnregisteredEntities() {
		return acceptUnregisteredEntities;
	}

	@Override
	public void setAcceptUnregisteredEntities(boolean acceptUnregisteredEntities) {
		this.acceptUnregisteredEntities = acceptUnregisteredEntities;
	}

	@Override
	public <T> Registration register(Class<T> type, Serializer<T> serializer, int id) {
		if (id < 100) {
			throw new RuntimeException("IDs < 100 are reserved");
		}
		return kryoConfig.getKryo().register(type, serializer, id);
	}

}
