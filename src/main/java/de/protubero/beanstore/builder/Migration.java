package de.protubero.beanstore.builder;

import java.util.function.Consumer;

class Migration {

	private String migrationId;
	private Consumer<MigrationTransaction> migration;

	public Migration(String migrationId, Consumer<MigrationTransaction> migration) {
		this.migrationId = migrationId;
		this.migration = migration;
	}

	public Consumer<MigrationTransaction> getMigration() {
		return migration;
	}

	public String getMigrationId() {
		return migrationId;
	}

}