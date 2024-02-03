package de.protubero.beanstore.builder.blocks;

import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.impl.BeanStoreTransactionImpl;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.pluginapi.BuilderTransactionListener;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.tx.Transaction;

public class StoreInitTool implements Consumer<StoreDataWriter> {

	public static final Logger log = LoggerFactory.getLogger(StoreInitTool.class);

	public static final String INIT_ID = "_INIT_";
	private Consumer<BeanStoreTransaction> initMigration;
	private String lastMigrationId;
	
	
	private StoreInitTool(String aLastMigrationId, Consumer<BeanStoreTransaction> initMigration) {
		this.initMigration = Objects.requireNonNull(initMigration);
		lastMigrationId = aLastMigrationId;
	}

	public StoreInitTool of(String aLastMigrationId, Consumer<BeanStoreTransaction> initMigration) {
		return new StoreInitTool(aLastMigrationId, initMigration);
	}

	public StoreInitTool of(String aLastMigrationId) {
		return new StoreInitTool(aLastMigrationId, (bst) -> {
		});
	}
	
	@Override
	public void accept(StoreDataWriter writer) {
		log.info("Init store");

		if (writer.getPhase() != StoreDataWriter.Phase.ImmutableBeans) {
			throw new AssertionError();
		}
		if (writer.getWrittenStates().size() != 0) {
			throw new AssertionError();
		}
		
		String initialTransactionId = INIT_ID;
		if (lastMigrationId != null) {
			initialTransactionId += lastMigrationId;
		}

		// always remember last migration id at the moment of creation
		var tx = Transaction.of(writer.companionSet(), initialTransactionId, PersistentTransaction.TRANSACTION_TYPE_MIGRATION);
		initMigration.accept(new BeanStoreTransactionImpl(tx));
		writer.execute(tx);
		
		if (tx.failed()) {
			throw new RuntimeException("Init store failed", tx.failure());
		}
	}
	
	
	
	
}
