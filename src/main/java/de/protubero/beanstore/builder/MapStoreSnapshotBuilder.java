package de.protubero.beanstore.builder;

import java.util.List;

import de.protubero.beanstore.api.BeanStoreSnapshot;
import de.protubero.beanstore.builder.blocks.BeanStoreState;
import de.protubero.beanstore.persistence.api.TransactionPersistence;

public interface MapStoreSnapshotBuilder {

	public static MapStoreSnapshotBuilder init(TransactionPersistence persistence) {
		return new MapStoreSnapshotBuilderImpl(persistence);
	}	
	
	BeanStoreSnapshot build();	

	BeanStoreSnapshot build(int state);	

	List<BeanStoreState> states();
	
}
