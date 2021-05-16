package de.protubero.beanstore.txmanager;

public interface DeferredTransactionExecutionContext {

	ExecutableBeanStoreTransaction transaction();
	
}
