package de.protubero.beanstore.persistence.api;

import java.time.Instant;

public class PersistentTransaction {

	public static final byte TRANSACTION_TYPE_DEFAULT = 0;
	public static final byte TRANSACTION_TYPE_MIGRATION = 1;
	
	
	private PersistentInstanceTransaction[] instanceTransactions;

	private Instant timestamp;
	private String transactionId;
	private byte transactionType = TRANSACTION_TYPE_DEFAULT;
	private int seqNum;
	

	public PersistentTransaction() {
	}
	
	public PersistentTransaction(byte transactionType, String transactionId) {
		this.transactionType = transactionType;
		this.transactionId = transactionId;
	}

	public PersistentInstanceTransaction[] getInstanceTransactions() {
		return instanceTransactions;
	}
	
	public void setInstanceTransactions(PersistentInstanceTransaction[] instanceTransactions) {
		this.instanceTransactions = instanceTransactions;
	}

	public PersistentInstanceTransaction create(String alias, Long id) {
		PersistentInstanceTransaction it = new PersistentInstanceTransaction();
		it.setAlias(alias);
		it.setId(id);
		it.setType(PersistentInstanceTransaction.TYPE_CREATE);
		
		append(it);
		return it;
	}
	
	public PersistentInstanceTransaction create(String alias, Long id, PersistentProperty ... propertyUpdates) {
		PersistentInstanceTransaction it = new PersistentInstanceTransaction();
		it.setAlias(alias);
		it.setId(id);
		it.setType(PersistentInstanceTransaction.TYPE_CREATE);
		it.setPropertyUpdates(propertyUpdates);
		
		append(it);
		return it;
	}

	public PersistentInstanceTransaction create(String alias, long id, PersistentProperty ... propertyUpdates) {
		PersistentInstanceTransaction it = new PersistentInstanceTransaction();
		it.setAlias(alias);
		it.setId(id);
		it.setType(PersistentInstanceTransaction.TYPE_CREATE);
		it.setPropertyUpdates(propertyUpdates);
		
		append(it);
		return it;
	}
	
	public PersistentInstanceTransaction update(String alias, long id, PersistentProperty ... propertyUpdates) {
		PersistentInstanceTransaction it = new PersistentInstanceTransaction();
		it.setAlias(alias);
		it.setId(id);
		it.setType(PersistentInstanceTransaction.TYPE_UPDATE);
		it.setPropertyUpdates(propertyUpdates);
		
		append(it);
		return it;
	}

	public PersistentInstanceTransaction delete(String alias, long id) {
		PersistentInstanceTransaction it = new PersistentInstanceTransaction();
		it.setAlias(alias);
		it.setId(id);
		it.setType(PersistentInstanceTransaction.TYPE_DELETE);
		
		append(it);
		return it;
	}
	
	private void append(PersistentInstanceTransaction it) {
		if (instanceTransactions == null) {
			instanceTransactions = new PersistentInstanceTransaction[] {it};
		} else {
			PersistentInstanceTransaction[] oldInstanceTransactions = instanceTransactions;
			instanceTransactions = new PersistentInstanceTransaction[oldInstanceTransactions.length + 1];
			System.arraycopy(oldInstanceTransactions, 0, instanceTransactions, 0, oldInstanceTransactions.length);
			instanceTransactions[instanceTransactions.length - 1] = it;
		}
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

	public byte getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(byte transactionType) {
		this.transactionType = transactionType;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	
}
