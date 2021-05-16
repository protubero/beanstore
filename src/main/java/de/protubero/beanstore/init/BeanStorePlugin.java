package de.protubero.beanstore.init;

import java.io.File;

import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.StoreSnapshot;
import de.protubero.beanstore.persistence.base.PersistentTransaction;
import de.protubero.beanstore.writer.BeanStoreChange;

public interface BeanStorePlugin {

	default void usedFile(File file) {
		
	}
	
	default void validate(AbstractPersistentObject apo) {
		
	}
	
	default void onStartCreate(BeanStoreFactory beanStoreFactory) {
		
	}
	
	default void onEndCreate(BeanStore beanStore, StoreSnapshot snapshot) {
		
	}
	
	default void onReadTransaction(PersistentTransaction transaction) {
		
	}
	
	default void onWriteTransaction(PersistentTransaction transaction) {
		
	}

	default void onInitTransaction(BeanStoreChange bsc) {
		
	}

	default void onMigrationTransaction(BeanStoreChange bsc) {
		
	}

		

}
