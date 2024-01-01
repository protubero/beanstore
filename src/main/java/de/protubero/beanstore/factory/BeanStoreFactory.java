package de.protubero.beanstore.factory;

import java.io.File;
import java.util.function.Consumer;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.persistence.api.KryoConfiguration;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;

/**
 * The factory class for BeanStore instances.
 * 
 */
public interface BeanStoreFactory {

	public static enum Mode {
		/**
		 * After migration only registered entities must exist
		 */
		RegisteredEntities, 
		
		/**
		 * Only MapObject companions allowed. Unknown entities from persistence will be added automatically
		 */
		LoadedMaps
	}
	
	
	/**
	 * The BeanStore created by this BeanStoreFactory will not store transactions.  
	 * 
	 * @return a BeanStore factory
	 */
	public static BeanStoreFactory createNonPersisted() {
		return new BeanStoreFactoryImpl();
	}
	
	/**
	 * The BeanStore created by this BeanStoreFactory stores transactions in the given file.  
	 * 
	 * @return a BeanStore factory
	 */
	public static BeanStoreFactory of(File file) {
		return new BeanStoreFactoryImpl(Mode.RegisteredEntities, file);
	}

	/**
	 * 
	 * 
	 * @param file
	 * @return
	 */
	public static BeanStoreFactory createMapStore(File file) {
		return new BeanStoreFactoryImpl(Mode.LoadedMaps, file);
	}
	
	
	/**
	 * Register a Java Bean entity. Remember: The class must inherit from AbstractEntity
	 * and is required to be annotated with the Entity annotation, which sets the <i>alias</i>
	 * of the entity. 
	 */
	<X extends AbstractEntity> BeanStoreEntity<X> registerEntity(Class<X> beanClass);
	
	
	void addMigration(String migrationId, Consumer<MigrationTransaction> migration);

	/**
	 * This callback code is only invoked if the store is completely new, i.e. if the
	 * file is created during the BeanStore create process..
	 */
	void initNewStore(Consumer<BeanStoreTransaction> initCallback);
	
	/**
	 * Register a BeanStore plugin.
	 */
	void addPlugin(BeanStorePlugin plugin);
	
	/**
	 * Create the bean store, i.e.
	 * <p>
	 * <ol>
	 * 	<li>Read all transactions from a file</li>
	 * 	<li>Apply necessary migrations to the data</li>
	 * 	<li>Convert map-based instances to bean-based instances</li>
	 * 	<li>Persist all transactions which were applied so far only in-memory</li>
	 * </ol>
	 * </p> 
	 */
	BeanStore create();

	MapObjectCompanion registerMapEntity(String alias);

	KryoConfiguration kryoConfig();
}
