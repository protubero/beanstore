package de.protubero.beanstore.plugins.history;

import java.util.function.Consumer;

import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.persistence.api.TransactionWriter;

public class HistoryPersistenceImpl implements TransactionPersistence {

	private BeanStoreHistoryPlugin history;
	
	@Override
	public TransactionReader reader() {
		return new TransactionReader() {
			
			@Override
			public void load(Consumer<PersistentTransaction> transactionConsumer) {
				
			}
		};
	}

	@Override
	public TransactionWriter writer() {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void onStartStoreBuild() {
		
	}

	@Override
	public Integer lastSeqNum() {
		return null;
	}

}