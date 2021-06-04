package de.protubero.beanstore.base.entity;

import java.util.Objects;

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
	
	public static InstanceKey of(String alias, long id) {
		Objects.requireNonNull(alias);
		Objects.requireNonNull(id);
		
		return new InstanceKey() {

			@Override
			public String alias() {
				return alias;
			}

			@Override
			public Long id() {
				return id;
			}
			
		};
	}
	
}
