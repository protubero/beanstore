package de.protubero.beanstore.builder.blocks;

import java.time.Instant;
import java.util.Optional;

import de.protubero.beanstore.persistence.api.PersistentTransaction;

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
	
	public Optional<String> migrationId() {
		if (transactionType == PersistentTransaction.TRANSACTION_TYPE_DEFAULT) {
			return Optional.empty();
		}
		if (transactionId.startsWith(StoreInitializer.INIT_ID)) {
			if (transactionId.equals(StoreInitializer.INIT_ID)) {
				return null;
			} else {
				return Optional.of(transactionId.substring(StoreInitializer.INIT_ID.length()));
			}
		} else {
			return Optional.of(transactionId);
		}
	}
	
	
}
