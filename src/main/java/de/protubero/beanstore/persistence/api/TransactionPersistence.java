package de.protubero.beanstore.persistence.api;

public interface TransactionPersistence {

	TransactionReader reader();
	
	TransactionWriter writer();
	
	boolean isEmpty();
}
