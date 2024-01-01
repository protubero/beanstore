package de.protubero.beanstore.persistence.api;

/**
 * A key/value pair, usually representing a property of an instance. 
 *
 */
public interface KeyValuePair {

	/**
	 * The property name (key) 
	 */
	String getProperty();
	
	/**
	 * The property value. 
	 * 
	 */
	Object getValue(); 
	
}
