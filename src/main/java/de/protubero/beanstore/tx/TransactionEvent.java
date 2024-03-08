package de.protubero.beanstore.tx;

import java.util.List;


public interface TransactionEvent {
	
	List<? extends InstanceTransactionEvent<?>> getInstanceEvents();
	
	default boolean success() {
		return !failed();
	}
	
	default boolean failed() {
		return failure() != null;
	}
	
	TransactionFailure failure();
	
	/**
	 * The transaction phase. 
	 */
	TransactionPhase phase();
	
	Integer getSourceStateVersion();
	
	Integer getTargetStateVersion();
	
	String getDescription();
	
}
