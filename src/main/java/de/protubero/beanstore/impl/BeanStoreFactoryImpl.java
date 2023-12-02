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

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreFactory;
import de.protubero.beanstore.api.BeanStorePlugin;
import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.api.MigrationTransaction;
import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.Transition;
import de.protubero.beanstore.base.entity.AbstractTaggedEntity;
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
import de.protubero.beanstore.persistence.impl.KryoPersistence;
import de.protubero.beanstore.store.EntityStore;
import de.protubero.beanstore.store.ImmutableEntityStore;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.txmanager.TaskQueueTransactionManager;
import de.protubero.beanstore.txmanager.TransactionManager;
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
	
	public BeanStoreFactoryImpl(File file) {
		this.file = Objects.requireNonNull(file);
	}
	
	public BeanStoreFactoryImpl() {
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
	
	@Override
	public BeanStore create() {
		MutableEntityStoreSet mapStore = new MutableEntityStoreSet(); 
		StoreWriter migrationStoreWriter = new StoreWriter(mapStore);
		
		
		throwExceptionIfAlreadyCreated();
		created = true;

		plugins.forEach(plugin -> plugin.onStartCreate(this));
		
		Runnable onCloseStoreAction = () -> {};
		Runnable onInitStoreAction = null;
		
				
		if (file != null) {
			plugins.forEach(plugin -> plugin.onOpenFile(file));
			
			// load transactions
			KryoPersistence persistence = new KryoPersistence(file);
			List<AppliedMigration> appliedMigrations = new ArrayList<>();
			boolean noStoredTransactions = persistence.isEmpty();
			if (!noStoredTransactions) {
				load(mapStore, persistence.reader(), appliedMigrations);
			}	
						
			DeferredTransactionWriter dtw = new DeferredTransactionWriter(persistence.writer());
			
			migrationStoreWriter.registerSyncInternalTransactionListener(TransactionPhase.PERSIST, t -> {
				plugins.forEach(plugin -> plugin.onWriteTransaction(t.persistentTransaction));
				dtw.append(t.persistentTransaction);
			});
			onCloseStoreAction = () -> {try {
				log.info("Closing transaction writer");
				dtw.close();
			} catch (Exception e) {
				log.error("Error closing transaction writer", e);
			}};			

			if (noStoredTransactions) {
				log.info("No store transactions");
				onInitStoreAction = () -> {
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
					var tx = Transaction.of(store, initialTransactionId, PersistentTransaction.TRANSACTION_TYPE_MIGRATION);
					initialMigration.accept(new BeanStoreTransactionImpl(tx));
					migrationStoreWriter.execute(tx);

					plugins.forEach(plugin -> plugin.onInitTransaction(tx));
				};	
				
			} else {
				// migrate store
				String databaseState = null;
				if (appliedMigrations.size() == 0) {
					throw new AssertionError("missing init migration");
				} else {
					log.info("No. of migration transations: " + appliedMigrations.size());
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
				
				// apply remaining migrations
				if (migrationStartIdx < migrations.size()) {
					for (int idx = migrationStartIdx; idx < migrations.size(); idx++) {
						var mig = migrations.get(idx);
						
						var tx = Transaction.of(store, mig.getMigrationId(), PersistentTransaction.TRANSACTION_TYPE_MIGRATION);
						mig.getMigration().accept(new MigrationTransactionImpl(tx));
						migrationStoreWriter.execute(tx);
						plugins.forEach(plugin -> plugin.onMigrationTransaction(tx));
						
						log.info("migration applied: " + mig.getMigrationId() + " (" + tx.getInstanceEvents().size() + ")");
					}
				}							
			}
			
			// convert maps to beans or create them new
			List<ImmutableEntityStore<?>> entityStoreList = new ArrayList<>();
			mapStore.forEach(es -> {
				Optional<Companion<AbstractPersistentObject>> registeredEntityCompanion = companionSet.companionByAlias(es.companion().alias());					
				if (registeredEntityCompanion.isPresent()) {
					Map<Long, AbstractPersistentObject> initialEntityMap = new HashMap<>();
					es.objects().forEach(obj -> {
						X newInstance = companion.newInstance();
						newInstance.id(obj.id());
						// copy all properties
						beanCompanion.transferProperties(obj, newInstance);
						
						// set tags ref to entity
						if (isTaggedEntity) {
							((AbstractTaggedEntity) newInstance).getTags().setEntity((AbstractTaggedEntity) newInstance);
						}
						
						newInstance.applyTransition(Transition.INSTANTIATED_TO_READY);
						
						if (callback != null) {
							callback.accept(newInstance);
						}
						newEntityStore.put(newInstance);
					});
					
				}
				
			});
			for (EntityCompanion<?> companion : entityCompanionMap.values()) {
				store.transformOrCreateBeanStore(companion, newBean -> {
					// validate all new beans
					plugins.forEach(plugin -> plugin.validate(newBean));
				});
			}
	
			// remove un-beaned entity stores
			for (EntityStore<?> es : store.entityStores()) {
				if (es.getCompanion() instanceof MapObjectCompanion) {
					store.removeMapStore(es);
				}
			}
			
			// init store
			if (onInitStoreAction != null) {
				onInitStoreAction.run();
			}
			
			// persist migration transactions
			// this is the first time that data gets written to the file 
			dtw.flush();
			dtw.deactivate();
		}

		TransactionManager finalTxManager = new TaskQueueTransactionManager(storeWriter);
		BeanStoreImpl beanStoreImpl = new BeanStoreImpl(finalTxManager, onCloseStoreAction);

		plugins.forEach(plugin -> plugin.onEndCreate(beanStoreImpl, new BeanStoreReadAccessImpl(store)));
		
		return beanStoreImpl;
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
				MutableEntityStore entityStore = (MutableEntityStore) store.storeOptional(pit.getAlias()).orElseGet(() -> {
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


	
	
}
