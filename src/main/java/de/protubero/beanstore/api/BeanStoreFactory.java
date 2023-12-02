package de.protubero.beanstore.api;

import java.io.File;
import java.util.function.Consumer;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.BeanStoreEntity;
import de.protubero.beanstore.impl.BeanStoreFactoryImpl;

/**
 * The factory class for BeanStore instances.
 * 
 */
public interface BeanStoreFactory {

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
		return new BeanStoreFactoryImpl(file);
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
	
	
	/**
	 * How to handle an entity that is found during the load process but has not been registered.
	 * If <i>false<i> (Default) an exception is thrown. If <i>true</i> the entity is kept as MapObject entity.
	 * 
	 * @return
	 */
	boolean isAcceptUnregisteredEntities();

	/**
	 * How to handle an entity that is found during the load process but has not been registered.
	 * If <i>false<i> (Default) an exception is thrown. If <i>true</i> the entity is kept as MapObject entity.
	 * 
	 * @param acceptUnregisteredEntities
	 */
	void setAcceptUnregisteredEntities(boolean acceptUnregisteredEntities);
	

}
