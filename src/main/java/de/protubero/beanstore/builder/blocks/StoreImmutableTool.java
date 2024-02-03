package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.EntityCompanion;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.ImmutableEntityStoreBase;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;

public class StoreImmutableTool implements Consumer<StoreDataWriter> {

	public static final Logger log = LoggerFactory.getLogger(StoreImmutableTool.class);
	
	
	private CompanionSet targetCompanionSet;

	public StoreImmutableTool(CompanionSet targetCompanionSet) {
		this.targetCompanionSet = Objects.requireNonNull(targetCompanionSet);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void accept(StoreDataWriter storeWriter) {
		List<ImmutableEntityStoreBase<?>> entityStoreBaseList = new ArrayList<>();
		
		// 1. iterate over loaded entities
		MutableEntityStoreSet mapStore = (MutableEntityStoreSet) storeWriter.getStore();
		
		for (MutableEntityStore<?> es : mapStore) {
			ImmutableEntityStoreBase<AbstractPersistentObject> newEntityStore = new ImmutableEntityStoreBase<>();
			entityStoreBaseList.add(newEntityStore);
			
			String entityAlias = es.companion().alias();	
			Optional<Companion<? extends AbstractPersistentObject>> registeredEntityCompanionOpt = targetCompanionSet.companionByAlias(entityAlias);
			if (registeredEntityCompanionOpt.isEmpty()) {
				if (es.isEmpty()) {
					log.info("Ignoring deleted entity " + entityAlias);
				} else {
					throw new RuntimeException("No registered entity matching loaded data: " + entityAlias);
				}	
			} else {
				final Companion<? extends AbstractPersistentObject> registeredEntityCompanion = registeredEntityCompanionOpt.get();
				newEntityStore.setNextInstanceId(es.getNextInstanceId());
				newEntityStore.setCompanion((Companion<AbstractPersistentObject>) registeredEntityCompanion);
				if (registeredEntityCompanion.isMapCompanion()) {
					newEntityStore.setObjectMap((Map<Long, AbstractPersistentObject>) es.getObjectMap());
				} else {
					// convert maps to entities 
					Map<Long, AbstractPersistentObject> initialEntityMap = new HashMap<>();
					es.objects().forEach(mapObj -> {
						AbstractEntity newInstance = (AbstractEntity) registeredEntityCompanion.createInstance();
						newInstance.id(mapObj.id());
						newInstance.state(State.PREPARE);
						// copy all properties
						((EntityCompanion<AbstractEntity>) registeredEntityCompanion).transferProperties((Map<String, Object>) mapObj, newInstance);

						newInstance.state(State.STORED);
						initialEntityMap.put(newInstance.id(), newInstance);
					});
					newEntityStore.setObjectMap(initialEntityMap);
				}
			}
		}	

		
		/**
		 * Add registered entities when there were no persisted data of these entities
		 */
		// 1. iterate over registered entities
		for (Companion<?> companion : targetCompanionSet) {
			// if alias not in list yet ...
			if (entityStoreBaseList.stream().filter(esb -> esb.getCompanion().alias().equals(companion.alias())).findAny().isEmpty()) {
				// .. add it
				ImmutableEntityStoreBase<AbstractPersistentObject> newEntityStore = new ImmutableEntityStoreBase<>();
				newEntityStore.setCompanion((Companion<AbstractPersistentObject>) companion);
				entityStoreBaseList.add(newEntityStore);
			}
		}

		
		// Create final store set
		ImmutableEntityStoreSet finalStoreSet = new ImmutableEntityStoreSet(
				entityStoreBaseList.toArray(new ImmutableEntityStoreBase[entityStoreBaseList.size()]), mapStore.version());
		
		storeWriter.setStore(finalStoreSet);
		storeWriter.switchToImmutableBeansPhase();
	}

}
