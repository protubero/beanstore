package de.protubero.beanstore.factory;

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
import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.EntityCompanion;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.impl.BeanStoreImpl;
import de.protubero.beanstore.impl.BeanStoreStateImpl;
import de.protubero.beanstore.impl.BeanStoreTransactionImpl;
import de.protubero.beanstore.persistence.api.KryoConfiguration;
import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.api.PersistentProperty;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.persistence.impl.DeferredTransactionWriter;
import de.protubero.beanstore.persistence.kryo.KryoConfigurationImpl;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;
import de.protubero.beanstore.pluginapi.FactoryTransactionListener;
import de.protubero.beanstore.pluginapi.PersistenceReadListener;
import de.protubero.beanstore.pluginapi.PersistenceWriteListener;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.ImmutableEntityStoreBase;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;
import de.protubero.beanstore.tx.InstanceTransactionEvent;
import de.protubero.beanstore.tx.StoreWriter;
import de.protubero.beanstore.tx.Transaction;
import de.protubero.beanstore.tx.TransactionPhase;

public class BeanStoreFactoryImpl implements BeanStoreFactory {

	public static final String INIT_ID = "_INIT_";

	public static final Logger log = LoggerFactory.getLogger(BeanStoreFactory.class);

	private Mode mode = Mode.RegisteredEntities;
	private List<Migration> migrations = new ArrayList<>();
	private Consumer<BeanStoreTransaction> initMigration;

	private CompanionSet companionSet = new CompanionSet();

	private boolean created;

	private List<BeanStorePlugin> plugins = new ArrayList<>();
	private List<FactoryTransactionListener> transactionListener = new ArrayList<>();
	private List<PersistenceReadListener> persistenceReadListener = new ArrayList<>();
	private List<PersistenceWriteListener> persistenceWriteListener = new ArrayList<>();

	private KryoConfiguration kryoConfig;
	private TransactionPersistence persistence;
	private DeferredTransactionWriter deferredTransactionWriter;
	private List<AppliedMigration> appliedMigrations = new ArrayList<>();

	
	public BeanStoreFactoryImpl(Mode mode, TransactionPersistence persistence) {
		this.persistence = Objects.requireNonNull(persistence);
		this.mode = Objects.requireNonNull(mode);
		
		kryoConfig = new KryoConfigurationImpl();
		this.persistence.kryoConfig(kryoConfig);
	}

	public BeanStoreFactoryImpl() {
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
		if (aPlugin instanceof FactoryTransactionListener) {
			transactionListener.add((FactoryTransactionListener) aPlugin);
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

		if (mode == Mode.LoadedMaps) {
			throw new RuntimeException("It is not allowed to register entity beans in LoadedMaps mode");
		}
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

	private void throwExceptionIfAlreadyCreated() {
		// It can only be created once
		if (created) {
			throw new RuntimeException("bean store has already been created");
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

		// call transaction listener
		for (FactoryTransactionListener listener : transactionListener) {
			listener.onInitTransaction(tx);
		}
		
		return aStoreSet;
	}

	private MutableEntityStoreSet loadMapStore() {
		// load transactions
		deferredTransactionWriter = new DeferredTransactionWriter(persistence.writer());

		boolean noStoredTransactions = persistence.isEmpty();
		if (noStoredTransactions) {
			return null;
		} else {
			MutableEntityStoreSet mapStore = new MutableEntityStoreSet();
			mapStore.setAcceptNonGeneratedIds(true);
			
			load(mapStore, persistence.reader(), appliedMigrations);

			return mapStore;
		}

	}

	private StoreWriter createStoreWriter() {
		StoreWriter storeWriter = new StoreWriter();

		storeWriter.registerSyncInternalTransactionListener(TransactionPhase.PERSIST, t -> {
			PersistentTransaction pTransaction = createTransaction(t);
			
			for (PersistenceWriteListener listener : persistenceWriteListener) {
				listener.onWriteTransaction(pTransaction);
			}
			if (deferredTransactionWriter != null) {
				deferredTransactionWriter.append(pTransaction);
			}
		});
		
		return storeWriter;
	}

	private PersistentTransaction createTransaction(Transaction transaction) {
		PersistentTransaction pt = new PersistentTransaction(transaction.getTransactionType(), transaction.getTransactionId());		
		pt.setTimestamp(Objects.requireNonNull(transaction.getTimestamp()));
		
		PersistentInstanceTransaction[] eventArray = new PersistentInstanceTransaction[transaction.getInstanceEvents().size()];
		int idx = 0;
		for (InstanceTransactionEvent<?> event : transaction.getInstanceEvents()) {
			PersistentInstanceTransaction pit = new PersistentInstanceTransaction();
			eventArray[idx++] = pit;
			pit.setAlias(event.entity().alias());
			switch (event.type()) {
			case Delete:
				pit.setType(PersistentInstanceTransaction.TYPE_DELETE);
				pit.setId(event.replacedInstance().id());
				pit.setVersion(event.replacedInstance().version());
				break;
			case Update:
				pit.setType(PersistentInstanceTransaction.TYPE_UPDATE);
				pit.setId(event.newInstance().id());
				pit.setPropertyUpdates((PersistentProperty[]) event.values());
				pit.setVersion(event.newInstance().version());
				
				break;
			case Create:
				pit.setType(PersistentInstanceTransaction.TYPE_CREATE);
				pit.setId(event.newInstance().id());
				pit.setPropertyUpdates((PersistentProperty[]) event.values());
				pit.setVersion(event.newInstance().version());

				break;
			}
		}
		pt.setInstanceTransactions(eventArray);
		
		return pt;
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
				mig.getMigration().accept(new MigrationTransactionImpl(tx, new BeanStoreStateImpl(migratedMapStore)));
				migratedMapStore = migrationStoreWriter.execute(tx, migratedMapStore);
				
				// call transaction listener
				for (FactoryTransactionListener listener : transactionListener) {
					listener.onMigrationTransaction(tx);
				}
				
				log.info("migration applied: " + mig.getMigrationId() + " (" + tx.getInstanceEvents().size() + ")");
			}
		}
		
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public BeanStore create() {
		throwExceptionIfAlreadyCreated();
		
		switch (mode) {
		case LoadedMaps:
			if (persistence == null) {
				throw new RuntimeException("Mode LoadedMaps and no peristence specified");
			}
			if (!companionSet.isEmpty()) {
				throw new RuntimeException("Mode LoadedMaps does not allow registered entities");
			}
			break;
		case RegisteredEntities:
			if (companionSet.isEmpty()) {
				throw new RuntimeException("Mode RegisteredEntities does not allow zero registered entities");
			}
			break;
		default:
			throw new AssertionError();
		}
		
		created = true;

		plugins.forEach(plugin -> plugin.onStartCreate(this));


		MutableEntityStoreSet mapStore = null;
		ImmutableEntityStoreSet finalStoreSet = null;
		if (persistence != null) {
			mapStore = loadMapStore();
			
			if (mapStore != null) {
				migrate(mapStore);
			} else if (mode == Mode.LoadedMaps) {
				throw new RuntimeException("Mode LoadedMaps but file does not exist");
			}
		}
	
		if (mapStore == null) {
			// i.e. either no file set or file does not exist
			finalStoreSet = new ImmutableEntityStoreSet(companionSet);
			finalStoreSet = initStore(finalStoreSet);
		} else {
			List<ImmutableEntityStoreBase<?>> entityStoreBaseList = new ArrayList<>();
			
			// 1. iterate over loaded entities
			for (MutableEntityStore<?> es : mapStore) {
				ImmutableEntityStoreBase<AbstractPersistentObject> newEntityStore = new ImmutableEntityStoreBase<>();
				entityStoreBaseList.add(newEntityStore);
				
				switch (mode) {
				case LoadedMaps:
					throw new RuntimeException("to be implemented");
//					newEntityStore.setCompanion(es.companion());
//					newEntityStore.setObjectMap(null);
				case RegisteredEntities:
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
					
					break;
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
							entityStoreBaseList.toArray(new ImmutableEntityStoreBase[entityStoreBaseList.size()]));
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

		plugins.forEach(plugin -> {
			plugin.onEndCreate(beanStoreImpl);
		});
		
		return beanStoreImpl;
	}


	@SuppressWarnings("unchecked")
	private void load(MutableEntityStoreSet store, TransactionReader reader, List<AppliedMigration> appliedMigrations) {
		reader.load(pt -> {

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
					instance.state(State.PREPARE);
					entityStore.put(instance);
					
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
					break;
				default:
					throw new AssertionError();
				}

				// update fields
				switch (pit.getType()) {
				case PersistentInstanceTransaction.TYPE_CREATE:
				case PersistentInstanceTransaction.TYPE_UPDATE:
					instance.version(pit.getVersion());
					if (pit.getPropertyUpdates() != null) {
						for (PersistentProperty propertyUpdate : pit.getPropertyUpdates()) {
							instance.put(propertyUpdate.getProperty(), propertyUpdate.getValue());
						}
					}
				}

			}
			
			for (PersistenceReadListener listener : persistenceReadListener) {
				listener.onReadTransaction(pt);
			}
		});
		
		// set state of all instances to 'Stored'
		store.forEach(es -> {
			es.objects().forEach(instance -> instance.state(State.STORED));
		});
	}

	public Mode getMode() {
		return mode;
	}

	@Override
	public void registerKryoPropertyBean(Class<?> propertyBeanClass) {
		throwExceptionIfAlreadyCreated();
		kryoConfig.register(propertyBeanClass);
	}

	@Override
	public <T> Registration registerKryoSerializer(Class<T> type, Serializer<T> serializer, int id) {
		throwExceptionIfAlreadyCreated();
		return kryoConfig.register(type, serializer, id);
	}

}
