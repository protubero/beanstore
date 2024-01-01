package de.protubero.beanstore.persistence.api;

public class PersistentBean {

	private String alias;
	private PersistentProperty[] properties;

	public PersistentProperty[] getProperties() {
		return properties;
	}

	public void setProperties(PersistentProperty[] properties) {
		this.properties = properties;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
}
