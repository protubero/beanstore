package de.protubero.beanstore.persistence.api;

public interface TransactionPersistence {

	void kryoConfig(KryoConfiguration kryoConfig);
	
	TransactionReader reader();
	
	TransactionWriter writer();
	
	boolean isEmpty();
}
