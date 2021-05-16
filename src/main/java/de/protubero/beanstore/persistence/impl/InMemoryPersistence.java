package de.protubero.beanstore.persistence.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import de.protubero.beanstore.persistence.api.PersistenceException;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.persistence.api.TransactionWriter;
import de.protubero.beanstore.persistence.base.PersistentTransaction;

public class InMemoryPersistence implements TransactionPersistence {

	private List<PersistentTransaction> transactionList = new ArrayList<>();

	private TransactionWriter writer;
	
	public InMemoryPersistence() {
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
	
	public TransactionReader reader() {
		return new TransactionReader() {
			
			@Override
			public void load(Consumer<PersistentTransaction> transactionConsumer) {
				transactionList.forEach(t -> transactionConsumer.accept(t));
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

	
}
