package de.protubero.beanstore.plugins.history;

import java.time.Instant;
import java.util.Map;

import de.protubero.beanstore.persistence.api.KeyValuePair;
import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;
import de.protubero.beanstore.tx.InstanceEventType;

public class InstanceState {

	private Map<String, Object> instance;
	private InstanceChange change;
	
	public InstanceState(Map<String, Object> instance, InstanceChange change) {
		this.instance = instance;
		this.change = change;
	}

	public long getId() {
		return change.getId();
	}

	public KeyValuePair[] getPropertyChanges() {
		return change.getPropertyChanges();
	}


	public Instant getTimestamp() {
		return change.getTimestamp();
	}


	public String getMigrationId() {
		return change.getMigrationId();
	}

	public byte getTransactionType() {
		return change.getTransactionType();
	}

	public String getAlias() {
		return change.getAlias();
	}

	public InstanceEventType getChangeType() {
		int changeType = change.getChangeType();
		if (changeType == PersistentInstanceTransaction.TYPE_CREATE) {
			return InstanceEventType.Create;
		} else if (changeType == PersistentInstanceTransaction.TYPE_UPDATE) {
			return InstanceEventType.Update;
		} else if (changeType == PersistentInstanceTransaction.TYPE_DELETE) {
			return InstanceEventType.Delete;
		} else {
			throw new AssertionError();
		}
	}

	public int getInstanceVersion() {
		return change.getInstanceVersion();
	}

	public int getStoreState() {
		return change.getStoreState();
	}

	public Map<String, Object> getInstance() {
		return instance;
	}

	
}
