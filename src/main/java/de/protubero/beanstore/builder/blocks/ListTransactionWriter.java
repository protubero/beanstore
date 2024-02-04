package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionWriter;

public class ListTransactionWriter implements TransactionWriter {

	private List<PersistentTransaction> transactions = new ArrayList<>();

	
	@Override
	public void close() throws Exception {
		// NOP
	}

	@Override
	public void append(Iterator<PersistentTransaction> aTransactions) {
		while (aTransactions.hasNext()) {
			transactions.add(aTransactions.next());
		}
	}

	@Override
	public void flush() {
		// NOP
	}

	public List<PersistentTransaction> getTransactions() {
		return transactions;
	}
	
	public void dump(TransactionWriter writer) {
		if (transactions.size() > 0) {
			writer.append(transactions.iterator());
		}
	}

}
