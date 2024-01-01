package de.protubero.beanstore.factory;

class AppliedMigration {

	private String migrationId;

	public AppliedMigration(String migrationId) {
		this.migrationId = migrationId;
	}

	public String getMigrationId() {
		return migrationId;
	}

}