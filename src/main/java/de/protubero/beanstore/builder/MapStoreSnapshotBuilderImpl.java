package de.protubero.beanstore.builder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStoreSnapshot;
import de.protubero.beanstore.builder.blocks.BeanStoreState;
import de.protubero.beanstore.builder.blocks.LoadedStoreData;
import de.protubero.beanstore.builder.blocks.StoreDataLoader;
import de.protubero.beanstore.impl.BeanStoreSnapshotImpl;
import de.protubero.beanstore.persistence.api.TransactionPersistence;

public class MapStoreSnapshotBuilderImpl  implements MapStoreSnapshotBuilder {

	public static final Logger log = LoggerFactory.getLogger(MapStoreSnapshotBuilderImpl.class);
	
	
	private TransactionPersistence persistence;
	
	MapStoreSnapshotBuilderImpl(TransactionPersistence persistence) {
		this.persistence = persistence;
	}

	@Override
	public BeanStoreSnapshot build(int state) {
		return build(Integer.valueOf(state));
	}

	@Override
	public List<BeanStoreState> states() {
		return StoreDataLoader.of(persistence).loadStates();
	}
	
	private BeanStoreSnapshot build(Integer state) {
		LoadedStoreData storeData = StoreDataLoader.of(persistence).load(state);
		if (storeData.store().isPresent() && state != null) {
			if (storeData.store().get().version() != state.intValue()) {
				throw new RuntimeException("Invalid state: " + state + ", max state is: " + storeData.store().get().version());
			}
		}
		return new BeanStoreSnapshotImpl(storeData.store().get());
	}

	@Override
	public BeanStoreSnapshot build() {
		return build(null);
	}

}
