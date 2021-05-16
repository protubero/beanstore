package de.protubero.beanstore.persistence.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.protubero.beanstore.persistence.base.PersistentTransaction;

public interface TransactionReader {

	void load(Consumer<PersistentTransaction> transactionConsumer);

	default List<PersistentTransaction> load() {
		List<PersistentTransaction> result = new ArrayList<>();		
		load(pt -> result.add(pt));		
		return result;
	}
	
	
}
