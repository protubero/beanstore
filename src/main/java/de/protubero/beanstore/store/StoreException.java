package de.protubero.beanstore.store;

public class StoreException extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -6265318430246485909L;

	public StoreException(String message) {
		super(message);
	}

	public StoreException(String message, Exception throwable) {
		super(message, throwable);
	}

	public StoreException(Exception throwable) {
		super(throwable);
	}

}
