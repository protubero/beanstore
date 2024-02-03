package de.protubero.beanstore.builder.blocks;

import java.util.function.Consumer;

import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.MutableEntityStoreSet;

public class StoreEnhanceTool implements Consumer<StoreDataWriter> {

	private CompanionSet targetCompanionSet;
	
	@Override
	public void accept(StoreDataWriter storeWriter) {
		MutableEntityStoreSet mapStore = (MutableEntityStoreSet) storeWriter.getStore();
		
		// fill up map store with registered entities without any persisted instances
		targetCompanionSet.companions().forEach(companion -> {
			if (mapStore.companionsShip().companionByAlias(companion.alias()).isEmpty()) {
				mapStore.register(new MapObjectCompanion(companion.alias()));
			}
		});
		
	}

}
