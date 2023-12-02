package de.protubero.beanstore.store;

import java.util.Objects;
import java.util.function.Consumer;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject.Transition;
import de.protubero.beanstore.base.entity.AbstractTaggedEntity;
import de.protubero.beanstore.base.entity.EntityCompanion;
import de.protubero.beanstore.base.entity.MapObjectCompanion;

public class StoreTransformer {

	private CompanionSet builder = new CompanionSet();
	private ImmutableEntityStoreSet targetStore;
	private MutableEntityStoreSet sourceStore;

	
	public StoreTransformer() {
	}

	
	public void transform(Consumer<X> callback) {
		
		EntityStore<?> origEntityStore = sourceStore.remove(beanCompanion.alias());
		if (origEntityStore != null) {
			if (!(origEntityStore.getCompanion() instanceof MapObjectCompanion)) {
				throw new StoreException("store with name " + beanCompanion.alias() + " is not a map store");
			}
		}	
		
		boolean isTaggedEntity = AbstractTaggedEntity.class.isAssignableFrom(beanCompanion.beanClass());
		
		// do the real conversion
		EntityStore<X> newEntityStore = targetStore.register(beanCompanion);
		
		if (origEntityStore != null) {
			// copy instances
			origEntityStore.objects().forEach(obj -> {
				X newInstance = newEntityStore.newInstance();
				newInstance.id(obj.id());
				// copy all properties
				beanCompanion.transferProperties(obj, newInstance);
				
				// set tags ref to entity
				if (isTaggedEntity) {
					((AbstractTaggedEntity) newInstance).getTags().setEntity((AbstractTaggedEntity) newInstance);
				}
				
				newInstance.applyTransition(Transition.INSTANTIATED_TO_READY);
				
				if (callback != null) {
					callback.accept(newInstance);
				}
				newEntityStore.put(newInstance);
			});
		}	
		
		return newEntityStore;
	}
	

}
