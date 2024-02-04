package de.protubero.beanstore.builder.blocks;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.impl.BeanStoreImpl;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionWriter;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.tx.StoreWriter;
import de.protubero.beanstore.tx.TransactionPhase;
import de.protubero.beanstore.tx.TxUtil;

public class BuildUtil {

	public static final Logger log = LoggerFactory.getLogger(BuildUtil.class);
	
	
	public static BeanStore build(ImmutableEntityStoreSet store, TransactionWriter writer, Consumer<PersistentTransaction> transactionListener) {
		StoreWriter storeWriter = new StoreWriter();
		storeWriter.registerSyncInternalTransactionListener(TransactionPhase.PERSIST, t -> {
			PersistentTransaction pTransaction = TxUtil.createPersistentTransaction(t);
			
			if (transactionListener != null) {
				transactionListener.accept(pTransaction);
			}
			
			writer.append(pTransaction);
		});
		
		
		Runnable onCloseAction = () -> {
			try {
				log.info("Closing transaction writer");
				writer.close();
			} catch (Exception e) {
				log.error("Error closing transaction writer", e);
			}
		};
		
		BeanStoreImpl beanStoreImpl = new BeanStoreImpl(store, onCloseAction, storeWriter);		
		return beanStoreImpl;
	}
	
}
