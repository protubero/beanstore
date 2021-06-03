package de.protubero.beanstore.impl;

import java.util.function.Consumer;

import de.protubero.beanstore.api.MigrationTransaction;

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