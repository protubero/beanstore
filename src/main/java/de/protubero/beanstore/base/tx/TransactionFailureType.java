package de.protubero.beanstore.base.tx;

public enum TransactionFailureType {
	INSTANCE_NOT_FOUND, 		
	OPTIMISTIC_LOCKING_FAILED, 		
	VERIFICATION_FAILED, 		
	PERSISTENCE_FAILED, 		
}