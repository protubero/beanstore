package de.protubero.beanstore.plugins.keyvalue;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;

public class KeyValueStore implements BeanStorePlugin {


	@Override
	public void onStartCreate(BeanStoreBuilder beanStoreBuilder) {
		beanStoreBuilder.registerEntity(KeyValueEntity.class);
		beanStoreBuilder.registerKryoSerializer(KeyObject.class, new KeyObjectSerializer(), 1001);
	}
	
	@Override
	public void onEndCreate(BeanStore beanStore) {
		for (KeyValueEntity kve : beanStore.snapshot().entity(KeyValueEntity.class)) {
			//kve.
		}
	}
}
