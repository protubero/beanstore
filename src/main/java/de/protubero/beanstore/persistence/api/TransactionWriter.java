package de.protubero.beanstore.persistence.api;

import java.util.Iterator;

import org.apache.commons.collections.iterators.SingletonIterator;

import de.protubero.beanstore.persistence.base.PersistentTransaction;

public interface TransactionWriter extends AutoCloseable {

	void append(Iterator<PersistentTransaction> transactions);

	void flush();
	
	@SuppressWarnings("unchecked")
	default void append(PersistentTransaction transaction) {
		append(new SingletonIterator(transaction));
	}
	
}
