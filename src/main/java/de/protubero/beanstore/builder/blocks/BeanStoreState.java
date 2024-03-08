package de.protubero.beanstore.builder.blocks;

import java.time.Instant;
import java.util.Optional;

import de.protubero.beanstore.persistence.api.PersistentTransaction;

public class BeanStoreState {

	private String migrationId;
	
	private Instant timestamp;
	
	private byte transactionType;
	
	private int state;
	
	private String description;

	public BeanStoreState(String migrationId, Instant timestamp, byte transactionType, int state, String aDescription) {
		this.migrationId = migrationId;
		this.timestamp = timestamp;
		this.transactionType = transactionType;
		this.state = state;
		this.description = aDescription;
	}

	
	
	public String getMigrationId() {
		return migrationId;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public byte getTransactionType() {
		return transactionType;
	}

	public int getState() {
		return state;
	}
	
	public String getDescription() {
		return description;
	}
	
	
}
