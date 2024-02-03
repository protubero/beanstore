package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.builder.MigrationTransactionImpl;
import de.protubero.beanstore.impl.BeanStoreSnapshotImpl;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.tx.Transaction;

public class StoreMigrationTool implements Consumer<StoreDataWriter> {

	public static final Logger log = LoggerFactory.getLogger(StoreMigrationTool.class);
	
		
	private List<Migration> migrations = new ArrayList<>();
	
	private StoreMigrationTool(List<Migration> migrations) {
		this.migrations = migrations;
	}
	
	public static StoreMigrationTool of(List<Migration> aMigrations) {
		return new StoreMigrationTool(aMigrations);
	}

	private String extractMigrationId(BeanStoreState state) {
		String txId = state.getTransactionId();
		if (txId.startsWith(StoreInitTool.INIT_ID)) {
			if (txId.equals(StoreInitTool.INIT_ID)) {
				return null;
			} else {
				return txId.substring(StoreInitTool.INIT_ID.length());
			}
		} else {
			return txId;
		}
	}
	
	@Override
	public void accept(StoreDataWriter storeWriter) {
		List<String> appliedMigrations = storeWriter.getWrittenStates().stream()
		.filter(state -> state.getTransactionType() == PersistentTransaction.TRANSACTION_TYPE_MIGRATION)
		.map(this::extractMigrationId)
		.collect(Collectors.toList());
		
		// migrate store		
		if (appliedMigrations.size() == 0) {
			throw new AssertionError("missing init migration");
		} else {
			log.info("No. of applied migration transactions (incl. init): " + appliedMigrations.size());
		}

		
		// find database state (i.e. last applied migration)
		String lastAppliedMigration = appliedMigrations.get(appliedMigrations.size() - 1);

		if (lastAppliedMigration == null) {
			if (appliedMigrations.size() != 1) {
				throw new AssertionError("unexpected");
			}
			log.info("loaded db state: INITIAL");
		} else {
			log.info("loaded db state: " + lastAppliedMigration);
		}

		int migrationStartIdx = 0;
		if (lastAppliedMigration != null) {
			// find migration which is referred to by the database state
			var tempIdx = 0;
			var lastMigrationIdx = -1;
			for (var mig : migrations) {
				if (mig.getMigrationId().equals(lastAppliedMigration)) {
					lastMigrationIdx = tempIdx;
					break;
				}
				tempIdx++;
			}
			if (lastMigrationIdx == -1) {
				throw new RuntimeException("missing migration id " + lastAppliedMigration);
			} else {
				migrationStartIdx = lastMigrationIdx + 1;
			}
		}

		
		// apply remaining migrations
		if (migrationStartIdx < migrations.size()) {
			for (int idx = migrationStartIdx; idx < migrations.size(); idx++) {
				var mig = migrations.get(idx);

				var tx = Transaction.of(storeWriter.companionSet(), mig.getMigrationId(),
						PersistentTransaction.TRANSACTION_TYPE_MIGRATION);
				mig.getMigration().accept(new MigrationTransactionImpl(tx, new BeanStoreSnapshotImpl(storeWriter.getStore())));

				storeWriter.execute(tx);
				
				if (tx.failed()) {
					throw new RuntimeException("Migration failed: " + mig.getMigrationId(), tx.failure());
				}
								
				log.info("migration applied: " + mig.getMigrationId() + " (" + tx.getInstanceEvents().size() + ")");
			}
		}
		
	}

	
}
