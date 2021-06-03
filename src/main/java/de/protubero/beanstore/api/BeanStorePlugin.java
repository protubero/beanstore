package de.protubero.beanstore.api;

import java.io.File;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.persistence.base.PersistentTransaction;

public interface BeanStorePlugin {

	default void onOpenFile(File file) {
		
	}
	
	default void validate(AbstractPersistentObject apo) {
		
	}
	
	default void onStartCreate(BeanStoreFactory beanStoreFactory) {
		
	}
	
	default void onEndCreate(BeanStore beanStore, BeanStoreReadAccess snapshot) {
		
	}
	
	default void onReadTransaction(PersistentTransaction transaction) {
		
	}
	
	default void onWriteTransaction(PersistentTransaction transaction) {
		
	}

	default void onInitTransaction(TransactionEvent bsc) {
		
	}

	default void onMigrationTransaction(TransactionEvent bsc) {
		
	}

		

}
