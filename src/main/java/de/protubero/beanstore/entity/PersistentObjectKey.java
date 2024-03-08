package de.protubero.beanstore.entity;

import java.util.Objects;

/**
 * The <i>complete</i> key of an instance, consisting of the entity alias and the id. 
 *
 */
public interface PersistentObjectKey {

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
	
	public static PersistentObjectKey of(String alias, long id) {
		Objects.requireNonNull(alias);
		
		return new PersistentObjectKey() {

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
