package de.protubero.beanstore.api;

public interface ExecutableLockedBeanStoreTransaction extends ExecutableBeanStoreTransaction {

	/**
	 * Access current persistent state of the store.
	 * 
	 */
	BeanStoreState lockedStoreState();	
	
}
