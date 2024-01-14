package de.protubero.beanstore.tx;

import java.util.List;


public interface TransactionEvent {
	
	List<? extends InstanceTransactionEvent<?>> getInstanceEvents();
	
	default boolean success() {
		return !failed();
	}
	
	boolean failed();
	
	TransactionFailure exception();
	
	/**
	 * The transaction phase. 
	 */
	TransactionPhase phase();
	
	Integer getTargetStateVersion();
}
