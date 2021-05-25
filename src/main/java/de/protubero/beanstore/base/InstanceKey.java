package de.protubero.beanstore.base;

/**
 * The <i>complete</i> key of an instance, consisting of the entity alias and the id. 
 *
 */
public interface InstanceKey {

	/**
	 * The entity alias. 
	 */
	String alias();
	
	/**
	 * The instance id. 
	 */
	Long id();
	
	/**
	 * Returns the key information as a string to be used e.g. for logging   
	 */
	default String toKeyString() {
		return alias() + "[" + id() + "]";
	}
	
}
