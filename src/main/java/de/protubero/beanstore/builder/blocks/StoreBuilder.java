package de.protubero.beanstore.builder.blocks;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.impl.BeanStoreImpl;

public class StoreBuilder implements Function<StoreDataWriter, BeanStore> {

	public static final Logger log = LoggerFactory.getLogger(StoreBuilder.class);
	
	
	@Override
	public BeanStore apply(StoreDataWriter storeDataWriter) {
		// persist migration transactions
		// this is the first time that data gets written to the file
		storeDataWriter.getDeferredTransactionWriter().switchToNonDeferred();

		Runnable onCloseStoreAction = () -> {try {
			log.info("Closing transaction writer");
			storeDataWriter.getDeferredTransactionWriter().close();
		} catch (Exception e) {
			log.error("Error closing transaction writer", e);
		}};			
		
		BeanStoreImpl beanStoreImpl = new BeanStoreImpl(finalStoreSet, onCloseStoreAction, storeDataWriter.getStoreWriter());
		return beanStoreImpl;
		
	}

}
