package de.protubero.beanstore.builder.blocks;

import java.time.Instant;

public class BeanStoreState {

	private String transactionId;
	
	public BeanStoreState(String transactionId, Instant timestamp, byte transactionType, int state) {
		this.transactionId = transactionId;
		this.timestamp = timestamp;
		this.transactionType = transactionType;
		this.state = state;
	}

	private Instant timestamp;
	
	private byte transactionType;
	
	private int state;

	public String getTransactionId() {
		return transactionId;
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
	
}
