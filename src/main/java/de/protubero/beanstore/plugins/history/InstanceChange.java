package de.protubero.beanstore.plugins.history;

import java.time.Instant;

import de.protubero.beanstore.base.tx.InstancePropertyValue;

public class InstanceChange {

	private int changeType;
	
	private long id;

	private String alias;
	
	private InstancePropertyValue[] propertyChanges;
	
	private Instant timestamp;

	private String transactionId;
	
	private int transactionType;
	
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public InstancePropertyValue[] getPropertyChanges() {
		return propertyChanges;
	}

	public void setPropertyChanges(InstancePropertyValue[] propertyChanges) {
		this.propertyChanges = propertyChanges;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public int getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(int transactionType) {
		this.transactionType = transactionType;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getChangeType() {
		return changeType;
	}

	public void setChangeType(int changeType) {
		this.changeType = changeType;
	}
	
}
