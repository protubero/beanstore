package de.protubero.beanstore.writer;

import java.util.List;

import de.protubero.beanstore.base.InstanceTransactionEvent;


public interface TransactionEvent {
	
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
	
	
}
