package de.protubero.beanstore.persistence.impl;

import java.util.Iterator;
import java.util.function.Consumer;

import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.persistence.api.TransactionWriter;

public class NoOpPersistence implements TransactionPersistence {

	public static NoOpPersistence create() {
		return new NoOpPersistence();
	}	
	
	@Override
	public TransactionReader reader() {
		return new TransactionReader() {
			
			@Override
			public void load(Consumer<PersistentTransaction> transactionConsumer) {
				// do nothing
			}
		};
	}

	@Override
	public TransactionWriter writer() {
		return new TransactionWriter() {
			
			@Override
			public void close() throws Exception {
				// do nothing
			}
			
			@Override
			public void flush() {
				// do nothing
			}
			
			@Override
			public void append(Iterator<PersistentTransaction> transactions) {
				// do nothing
			}
		};
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public void lockConfiguration() {
		// NOP
	}

}
