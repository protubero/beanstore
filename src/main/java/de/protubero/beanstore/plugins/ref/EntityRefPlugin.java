package de.protubero.beanstore.plugins.ref;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;

public class EntityRefPlugin implements BeanStorePlugin {

	@Override
	public void onEndCreate(BeanStore beanStore) {
		beanStore.state().entity(EntityRelation.class).forEach(er -> {
			
		});
		
		beanStore.callbacks().onChangeInstance(EntityRelation.class, ite -> {
			switch (ite.type()) {
			case Create: 
			case Update:
				// do nothing
				break;
			case Delete:
				
				break;
			}
		});
	}
	
	// allRefs from and to an entity
	
	
	// filter entity by Ref Predicate 
	
	
}
