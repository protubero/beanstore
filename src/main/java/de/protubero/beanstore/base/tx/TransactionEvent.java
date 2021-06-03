package de.protubero.beanstore.base.tx;

import java.util.List;


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
