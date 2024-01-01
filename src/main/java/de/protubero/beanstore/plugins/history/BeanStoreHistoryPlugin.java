package de.protubero.beanstore.plugins.history;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;
import de.protubero.beanstore.pluginapi.PersistenceReadListener;
import de.protubero.beanstore.pluginapi.PersistenceWriteListener;

public class BeanStoreHistoryPlugin implements BeanStorePlugin, PersistenceReadListener, PersistenceWriteListener {

	private List<InstanceChange> changes = new ArrayList<>();
	
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
				InstanceChange change = new InstanceChange();
				
				change.setId(it.getId().longValue());
				change.setTimestamp(transaction.getTimestamp());
				change.setTransactionId(transaction.getTransactionId());
				change.setTransactionType(transaction.getTransactionType());
				change.setChangeType(it.getType());
				change.setPropertyChanges(it.getPropertyUpdates());
				change.setAlias(it.getAlias());
				
				changes.add(change);
			}
		}
	}
	
	public List<InstanceChange> changes(String alias, long id) {
		return changes.stream().filter(it -> it.getAlias().equals(alias) && it.getId() == id).collect(Collectors.toList());
	}
	
}
