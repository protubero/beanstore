package de.protubero.beanstore.builder;

import java.util.function.Consumer;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.entity.MapObject;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;

/**
 * The builder class for BeanStore instances.
 * 
 */
public interface BeanStoreBuilder {


	public static BeanStoreBuilder init(TransactionPersistence persistence) {
		return new BeanStoreBuilderImpl(persistence);
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
	BeanStore build();

	BeanStoreEntity<MapObject> registerMapEntity(String alias);

	boolean isAutoCreateEntities();

	void setAutoCreateEntities(boolean autoCreateEntities);

}