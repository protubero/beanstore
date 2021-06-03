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
	
	<X extends AbstractEntity> BeanStoreEntity<X> registerType(Class<X> beanClass);

	void addMigration(String migrationId, Consumer<MigrationTransaction> migration);
		
	void initNewStore(Consumer<BeanStoreTransaction> migration);
	
	void addPlugin(BeanStorePlugin plugin);
	
	BeanStore create();

	

}
