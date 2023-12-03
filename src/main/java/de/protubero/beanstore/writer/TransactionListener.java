package de.protubero.beanstore.writer;

import de.protubero.beanstore.store.ImmutableEntityStoreSet;

public interface TransactionListener {

	void onTransactionExecuted(ImmutableEntityStoreSet source, ImmutableEntityStoreSet target);
	
}
