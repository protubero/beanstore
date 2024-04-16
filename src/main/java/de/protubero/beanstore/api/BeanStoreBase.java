package de.protubero.beanstore.api;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.CompanionRegistry;
import de.protubero.beanstore.entity.EntityCompanion;
import de.protubero.beanstore.entity.Keys;
import de.protubero.beanstore.tx.Transaction;

public interface BeanStoreBase {


	/**
	 * Meta information of the store, i.e. information about the entities in the store. 
	 */
	BeanStoreMetaInfo meta();
	
	/**
	 * Access current persistent state of the store.
	 * 
	 * @return a BeanStoreState instance
	 */
	BeanStoreSnapshot snapshot();
	
	
	/**
	 * Create a new executable transaction. 
	 * 
	 * @return a transaction
	 */
	ExecutableBeanStoreTransaction transaction();
	
	ExecutableBeanStoreTransaction transaction(String description);
	
    @SuppressWarnings("unchecked")
	default <T extends AbstractEntity> T create(AbstractEntity entity) {
    	var tx = transaction();
    	tx.create(entity);
    	return (T) tx.execute().getInstanceEvents().get(0).newInstance();
    }

    default <T extends AbstractEntity> BeanStoreTransactionResult update(Class<T> beanClass, long id, Consumer<T> consumer) {
    	var tx = transaction();
    	T recInstance = tx.update(Keys.key(beanClass, id));
    	consumer.accept(recInstance);
    	return tx.execute();
    }

    default <T extends AbstractEntity> BeanStoreTransactionResult update(Class<T> beanClass, long id, Integer version, Map<String, Object> updatedFields) {
    	if (updatedFields.isEmpty()) {
    		throw new RuntimeException("Update map with no entries");
    	}

    	Optional<EntityCompanion<T>> companionOpt = CompanionRegistry.getEntityCompanionByClass(beanClass);
    	if (companionOpt.isEmpty()) {
    		throw new RuntimeException("Not an entity class: " + beanClass);
    	}	
    	EntityCompanion<T> companion = companionOpt.get();
    	
    	var tx = transaction();
    	T recInstance = startTransaction(tx, beanClass, id, version);
    	
    	updatedFields.entrySet().forEach(entry -> {
    		PropertyDescriptor propDesc = companion.propertyDescriptorOf(entry.getKey());
    		if (propDesc == null) {
    			throw new RuntimeException("Invalid field name: " + entry.getKey());
    		}
    		try {
    			propDesc.getWriteMethod().invoke(recInstance, entry.getValue());
    		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
    			throw new RuntimeException("Error setting value", e);
    		}
    	});
    	
    	return tx.execute();
    }

    private <T extends AbstractEntity> T startTransaction(ExecutableBeanStoreTransaction tx, Class<T> beanClass, long id, Integer version) {
    	if (version != null) {
    		return tx.update(Keys.versionKey(beanClass, id, version.intValue()));
    	} else {
    		return tx.update(Keys.key(beanClass, id));
    	}	
    }

	default <T extends AbstractEntity> BeanStoreTransactionResult delete(Class<T> entityClass, Long id, Integer version) {
    	var tx = transaction();
    	if (version != null) {
        	tx.delete(Keys.versionKey(entityClass, id, version.intValue()));
    	} else {
        	tx.delete(Keys.key(entityClass, id));
    	}	

    	return tx.execute();
    }		
	
     default <T extends AbstractEntity> BeanStoreTransactionResult delete(T entityInstance) {
    	var tx = transaction();
    	tx.delete(Keys.key(entityInstance));
    	return tx.execute();
    }
     
     default <T extends AbstractEntity> BeanStoreTransactionResult deleteOptLocked(T entityInstance) {
    	var tx = transaction();
    	tx.delete(Keys.versionKey(entityInstance));
    	return tx.execute();
    }		
     
}
