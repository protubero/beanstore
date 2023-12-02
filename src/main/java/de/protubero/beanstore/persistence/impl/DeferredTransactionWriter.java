package de.protubero.beanstore.persistence.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.protubero.beanstore.persistence.api.TransactionWriter;
import de.protubero.beanstore.persistence.base.PersistentTransaction;

public class DeferredTransactionWriter implements TransactionWriter {

	private TransactionWriter writer;
	private List<PersistentTransaction> transactions = new ArrayList<>();
	private boolean deferralActive = true;
	
	public DeferredTransactionWriter(TransactionWriter writer) {
		this.writer = writer;
	}

	@Override
	public void close() throws Exception {
		writer.close();
	}

	@Override
	public void append(Iterator<PersistentTransaction> aTransactions) {
		if (deferralActive) {
			while (aTransactions.hasNext()) {
				transactions.add(aTransactions.next());
			}
		} else {
			writer.append(aTransactions);
		}
	}
	
	public void switchToNonDeferred() {
		flush();
		writeTransactions();	
		deferralActive = false;
	}

	@Override
	public void flush() {
		writeTransactions();	
		writer.flush();
	}

	private void writeTransactions() {
		if (transactions.size() > 0) {
			writer.append(transactions.iterator());
			transactions.clear();
		}
	}

}
