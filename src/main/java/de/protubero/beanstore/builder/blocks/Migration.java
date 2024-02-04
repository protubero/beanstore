package de.protubero.beanstore.builder.blocks;

import java.util.Objects;
import java.util.function.Consumer;

import de.protubero.beanstore.builder.MigrationTransaction;

public class Migration {

	private String migrationId;
	private Consumer<MigrationTransaction> migration;

	public Migration(String aMigrationId, Consumer<MigrationTransaction> migration) {
		this.migrationId = Objects.requireNonNull(aMigrationId);
		this.migration = Objects.requireNonNull(migration);
		
		if (!migrationId.trim().equals(migrationId)) {
			throw new RuntimeException("invalid migration id");
		}
	}
	
	public Consumer<MigrationTransaction> getMigration() {
		return migration;
	}

	public String getMigrationId() {
		return migrationId;
	}
	
	

}