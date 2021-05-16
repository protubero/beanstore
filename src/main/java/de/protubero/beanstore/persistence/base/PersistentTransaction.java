package de.protubero.beanstore.persistence.base;

import java.time.Instant;

public class PersistentTransaction {

	public static final int TRANSACTION_TYPE_DEFAULT = 0;
	public static final int TRANSACTION_TYPE_MIGRATION = 1;
	
	
	private PersistentInstanceTransaction[] instanceTransactions;

	private Instant timestamp;
	private String transactionId;
	private int transactionType = TRANSACTION_TYPE_DEFAULT;
	

	public PersistentTransaction() {
	}
	
	public PersistentTransaction(int transactionType, String transactionId) {
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
	
	public PersistentInstanceTransaction create(String alias, Long id, PersistentPropertyUpdate ... propertyUpdates) {
		PersistentInstanceTransaction it = new PersistentInstanceTransaction();
		it.setAlias(alias);
		it.setId(id);
		it.setType(PersistentInstanceTransaction.TYPE_CREATE);
		it.setPropertyUpdates(propertyUpdates);
		
		append(it);
		return it;
	}

	public PersistentInstanceTransaction create(String alias, long id, PersistentPropertyUpdate ... propertyUpdates) {
		PersistentInstanceTransaction it = new PersistentInstanceTransaction();
		it.setAlias(alias);
		it.setId(id);
		it.setType(PersistentInstanceTransaction.TYPE_CREATE);
		it.setPropertyUpdates(propertyUpdates);
		
		append(it);
		return it;
	}
	
	public PersistentInstanceTransaction update(String alias, long id, PersistentPropertyUpdate ... propertyUpdates) {
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

	public int getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(int transactionType) {
		this.transactionType = transactionType;
	}

	
}
