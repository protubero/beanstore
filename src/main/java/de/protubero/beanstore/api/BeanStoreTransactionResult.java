package de.protubero.beanstore.api;

import java.util.List;

import de.protubero.beanstore.base.tx.InstanceTransactionEvent;
import de.protubero.beanstore.base.tx.TransactionFailure;
import de.protubero.beanstore.base.tx.TransactionPhase;

public interface BeanStoreTransactionResult {
	
	List<InstanceTransactionEvent<?>> getInstanceEvents();
	
	default boolean success() {
		return !failed();
	}
	
	boolean failed();
	
	TransactionFailure exception();
	
	/**
	 * The transaction phase. 
	 */
	TransactionPhase phase();
	
	
	BeanStoreState baseStoreState();

	BeanStoreState resultStoreState();
	
}