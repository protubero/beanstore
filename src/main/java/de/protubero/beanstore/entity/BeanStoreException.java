package de.protubero.beanstore.entity;

public class BeanStoreException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3477933718280047684L;

	public BeanStoreException() {
	}
	
	public BeanStoreException(String message) {
		super(message);
	}

	public BeanStoreException(Exception e) {
		super(e);
	}
	
}
