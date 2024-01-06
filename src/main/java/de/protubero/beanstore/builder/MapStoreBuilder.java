package de.protubero.beanstore.builder;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.persistence.api.TransactionPersistence;

public interface MapStoreBuilder {

	public static MapStoreBuilder init(TransactionPersistence persistence) {
		return new MapStoreBuilderImpl(persistence);
	}
	
	
	BeanStore build();

	
}
