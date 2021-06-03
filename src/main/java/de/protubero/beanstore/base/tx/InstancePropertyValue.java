package de.protubero.beanstore.base.tx;

/**
 * A key/value pair, usually representing a property of an instance. 
 *
 */
public interface InstancePropertyValue {

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
