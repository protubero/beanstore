package de.protubero.beanstore.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreSnapshot;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.impl.BeanStoreImpl;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.store.CompanionSetImpl;
import de.protubero.beanstore.store.ImmutableEntityStoreBase;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;

public class MapStoreSnapshotBuilderImpl /*extends AbstractStoreBuilder implements MapStoreSnapshotBuilder*/ {

	public static final Logger log = LoggerFactory.getLogger(MapStoreSnapshotBuilderImpl.class);
	
	/*
	public MapStoreSnapshotBuilderImpl(TransactionPersistence persistence) {
		super(persistence);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public BeanStoreSnapshot build() {
		
		startBuildProcess();
		
		MutableEntityStoreSet mapStore = loadMapStore();
		ImmutableEntityStoreSet finalStoreSet = null;

	
		if (mapStore == null) {
			// i.e. either no file set or file does not exist
			finalStoreSet = new ImmutableEntityStoreSet(new CompanionSetImpl(), 0);
		} else {
			List<ImmutableEntityStoreBase<?>> entityStoreBaseList = new ArrayList<>();
			
			// 1. iterate over loaded entities
			for (MutableEntityStore<?> es : mapStore) {
				ImmutableEntityStoreBase<AbstractPersistentObject> newEntityStore = new ImmutableEntityStoreBase<>();
				entityStoreBaseList.add(newEntityStore);
				newEntityStore.setNextInstanceId(es.getNextInstanceId());
				newEntityStore.setCompanion((Companion<AbstractPersistentObject>) es.companion());
				newEntityStore.setObjectMap((Map<Long, AbstractPersistentObject>) es.getObjectMap());
			}	

			
			// Create final store set
			finalStoreSet = 
					new ImmutableEntityStoreSet(
							entityStoreBaseList.toArray(new ImmutableEntityStoreBase[entityStoreBaseList.size()]), mapStore.version());
		}

		
		BeanStoreImpl beanStoreImpl = endBuildProcess(finalStoreSet);
		
		return beanStoreImpl;
	}


	@Override
	protected void onReadMigrationTransaction(PersistentTransaction pt) {
		// NOOP
	}

	@Override
	protected void onReadTransaction(PersistentTransaction pt) {
		// NOOP
	}

	@Override
	protected void onWriteTransaction(PersistentTransaction pt) {
		// NOOP
	}

	@Override
	public BeanStoreSnapshot build(int state) {
		return null;
	}
*/
}
