package de.protubero.beanstore.plugins.search;

public class SearchEngineException extends RuntimeException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1716312580194370579L;

	public SearchEngineException(String message) {
		super(message);
	}
	
	public SearchEngineException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchEngineException(Throwable e) {
		super(e);
	}
}
