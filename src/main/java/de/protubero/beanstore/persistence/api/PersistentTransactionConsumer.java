package de.protubero.beanstore.persistence.api;

import java.util.function.Consumer;

public interface PersistentTransactionConsumer extends Consumer<PersistentTransaction> {

	default boolean wantsNextTransaction() {
		return true;
	}
	
}
