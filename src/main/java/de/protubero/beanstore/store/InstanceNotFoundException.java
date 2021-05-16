package de.protubero.beanstore.store;

public class InstanceNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1556272808787859126L;

	private String alias;
	private Long id;
	
	public InstanceNotFoundException(String alias, Long id) {
		super("Instance not found: " + alias + "[" + id  + "]");
		
		this.alias = alias;
		this.id = id;
	}

	public String getAlias() {
		return alias;
	}

	public Long getId() {
		return id;
	}	
}
