package de.protubero.beanstore.plugins.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.CompanionRegistry;
import de.protubero.beanstore.entity.EntityCompanion;
import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;
import de.protubero.beanstore.pluginapi.PersistenceReadListener;
import de.protubero.beanstore.pluginapi.PersistenceWriteListener;

public class BeanStoreHistoryPlugin implements BeanStorePlugin, PersistenceReadListener, PersistenceWriteListener {

	
	private Map<String, List<InstanceChange>> map = new HashMap<>();
	
	public void register(String ... types) {
		for (String type : types) {
			map.put(type, new ArrayList<>());
		}
	}
	
	@Override
	public void onReadTransaction(PersistentTransaction transaction) {
		add(transaction);
	}
	
	@Override
	public void onWriteTransaction(PersistentTransaction transaction) {
		add(transaction);
	}

	private void add(PersistentTransaction transaction) {
		if (transaction.getInstanceTransactions() != null) {
			for (var it : transaction.getInstanceTransactions()) {
				List<InstanceChange> changes = map.get(it.getAlias());
				
				if (changes != null) {
					InstanceChange change = new InstanceChange();
					
					change.setId(it.getId().longValue());
					change.setTimestamp(transaction.getTimestamp());
					change.setMigrationId(transaction.getMigrationId());
					change.setTransactionType(transaction.getTransactionType());
					change.setChangeType(it.getType());
					change.setPropertyChanges(it.getPropertyUpdates());
					change.setAlias(it.getAlias());
					change.setInstanceVersion(it.getVersion());
					change.setStoreState(transaction.getSeqNum());
					
					changes.add(change);
				}	
			}
		}
	}
	
	public <T extends AbstractPersistentObject> List<InstanceState> changes(PersistentObjectKey<T> key) {
		String alias = key.alias();
		if (alias == null) {
			@SuppressWarnings({ "unchecked" })
			EntityCompanion<AbstractEntity> companion = CompanionRegistry.getEntityCompanionByClass((Class<AbstractEntity>) key.entityClass()).get();
			alias = companion.alias();
		}
		
		List<InstanceChange> changes = map.get(alias);
		if (changes == null) {
			throw new RuntimeException("unregistered entity type " + alias);
		}

		HistoryBuilder builder = new HistoryBuilder();
		changes.stream()
		.filter(it -> it.getId() == key.id())
		.forEach(builder);
		
		return builder.getStates();
	}
	
}
