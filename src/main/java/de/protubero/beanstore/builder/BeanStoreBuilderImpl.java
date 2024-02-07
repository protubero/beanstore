package de.protubero.beanstore.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.builder.blocks.InterimStore;
import de.protubero.beanstore.builder.blocks.LoadedStoreData;
import de.protubero.beanstore.builder.blocks.Migration;
import de.protubero.beanstore.builder.blocks.StoreDataLoader;
import de.protubero.beanstore.builder.blocks.StoreInitialization;
import de.protubero.beanstore.builder.blocks.StoreInitializer;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;
import de.protubero.beanstore.pluginapi.PersistenceReadListener;
import de.protubero.beanstore.pluginapi.PersistenceWriteListener;

public class BeanStoreBuilderImpl implements BeanStoreBuilder {

	public static final Logger log = LoggerFactory.getLogger(BeanStoreBuilderImpl.class);

	private StoreInitialization initialization = new StoreInitialization();	
	protected TransactionPersistence persistence;
	protected boolean created;
	
	private List<BeanStorePlugin> plugins = new ArrayList<>();
	private List<PersistenceReadListener> persistenceReadListener = new ArrayList<>();
	private List<PersistenceWriteListener> persistenceWriteListener = new ArrayList<>();

	
	BeanStoreBuilderImpl(TransactionPersistence aPersistence) {
		persistence = aPersistence;
	}

	protected void throwExceptionIfAlreadyCreated() {
		// It can only be created once
		if (created) {
			throw new RuntimeException("bean store has already been created");
		}
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
		return initialization.registerEntity(beanClass);
	}

	@Override
	public MapObjectCompanion registerMapEntity(String alias) {
		throwExceptionIfAlreadyCreated();

		return initialization.registerMapEntity(alias);
	}
	
	@Override
	public void addMigration(String migrationId, Consumer<MigrationTransaction> migration) {
		throwExceptionIfAlreadyCreated();

		initialization.addMigration(new Migration(migrationId, migration));
	}

	@Override
	public void initNewStore(Consumer<BeanStoreTransaction> migration) {
		throwExceptionIfAlreadyCreated();
		
		initialization.initNewStore(migration);
	}

	@Override
	public BeanStore build() {
		throwExceptionIfAlreadyCreated();

		initialization.check();
		
		created = true;
		
		plugins.forEach(plugin -> plugin.onStartCreate(this));

		LoadedStoreData storeData = StoreDataLoader.of(persistence, tx -> {
			for (PersistenceReadListener listener : persistenceReadListener) {
				listener.onReadTransaction(tx);
			}
		}).load(null);

		InterimStore interimStore = InterimStore.of(storeData, tx -> {
			for (PersistenceWriteListener listener : persistenceWriteListener) {
				listener.onWriteTransaction(tx);
			}
		});
		
		StoreInitializer.of(initialization).accept(interimStore);
		
		BeanStore beanStoreImpl = interimStore.build();
		
		plugins.forEach(plugin -> {
			plugin.onEndCreate(beanStoreImpl);
		});
		
		return beanStoreImpl;
	}

	@Override
	public boolean isAutoCreateEntities() {
		return initialization.isAutoCreateEntities();
	}

	@Override
	public void setAutoCreateEntities(boolean autoCreateEntities) {
		this.initialization.setAutoCreateEntities(autoCreateEntities);
	}


}
