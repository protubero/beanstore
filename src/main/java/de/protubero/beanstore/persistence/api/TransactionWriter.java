package de.protubero.beanstore.persistence.api;

import java.util.Iterator;

import org.apache.commons.collections.iterators.SingletonIterator;

public interface TransactionWriter extends AutoCloseable {
	
	void append(Iterator<PersistentTransaction> transactions);

	void flush();
	
	@SuppressWarnings("unchecked")
	default void append(PersistentTransaction transaction) {
		append(new SingletonIterator(transaction));
	}
	
}
