package de.protubero.beanstore.impl;

class AppliedMigration {

	private String migrationId;

	public AppliedMigration(String migrationId) {
		this.migrationId = migrationId;
	}

	public String getMigrationId() {
		return migrationId;
	}

}