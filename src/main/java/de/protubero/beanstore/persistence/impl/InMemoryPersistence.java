package de.protubero.beanstore.persistence.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.protubero.beanstore.persistence.api.PersistenceException;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.PersistentTransactionConsumer;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.persistence.api.TransactionWriter;

public class InMemoryPersistence implements TransactionPersistence {

	private List<PersistentTransaction> transactionList = new ArrayList<>();

	private TransactionWriter writer;
	
	InMemoryPersistence() {
		writer = new TransactionWriter() {

			boolean closed;
			
			@Override
			public void append(Iterator<PersistentTransaction> transactions) {
				if (closed) {
					throw new PersistenceException("writing to a closed writer");
				}
				while (transactions.hasNext()) {
					transactionList.add(transactions.next());
				}
			}

			@Override
			public void close() throws Exception {
				closed = true;
			}

			@Override
			public void flush() {
				// nothing to do
			}
			
		};

	}
	
	public static InMemoryPersistence create() {
		return new InMemoryPersistence();
	}
	
	public TransactionReader reader() {
		return new TransactionReader() {
			
			@Override
			public void load(PersistentTransactionConsumer transactionConsumer) {
				for (PersistentTransaction pt : transactionList) {
					if (!transactionConsumer.wantsNextTransaction()) {
						return;
					}
					transactionConsumer.accept(pt);
				}
			}
		};
	}

	public TransactionWriter writer() {
		return writer;
	}


	public List<PersistentTransaction> getTransactionList() {
		return transactionList;
	}

	@Override
	public boolean isEmpty() {
		return transactionList.size() == 0;
	}

	@Override
	public void onStartStoreBuild() {
		// NOP
	}

	@Override
	public Integer lastSeqNum() {
		if (transactionList.isEmpty()) {
			return null;
		}
		return transactionList.get(transactionList.size() - 1).getSeqNum();
	}


	
}
