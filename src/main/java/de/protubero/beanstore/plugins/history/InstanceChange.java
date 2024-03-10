package de.protubero.beanstore.plugins.history;

import java.time.Instant;

import de.protubero.beanstore.persistence.api.KeyValuePair;

public class InstanceChange {

	private byte changeType;
	
	private int instanceVersion;

	private int storeState;
	
	private long id;

	private String alias;
	
	private KeyValuePair[] propertyChanges;
	
	private Instant timestamp;

	private String migrationId;
	
	private byte transactionType;
	
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public KeyValuePair[] getPropertyChanges() {
		return propertyChanges;
	}

	public void setPropertyChanges(KeyValuePair[] propertyChanges) {
		this.propertyChanges = propertyChanges;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getMigrationId() {
		return migrationId;
	}

	public void setMigrationId(String migrationId) {
		this.migrationId = migrationId;
	}

	public byte getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(byte transactionType) {
		this.transactionType = transactionType;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public byte getChangeType() {
		return changeType;
	}

	public void setChangeType(byte changeType) {
		this.changeType = changeType;
	}

	public int getInstanceVersion() {
		return instanceVersion;
	}

	public void setInstanceVersion(int instanceVersion) {
		this.instanceVersion = instanceVersion;
	}

	public int getStoreState() {
		return storeState;
	}

	public void setStoreState(int storeState) {
		this.storeState = storeState;
	}

	
}
