package de.protubero.beanstore.entity;

import java.util.Objects;

public interface PersistentObjectVersionKey extends PersistentObjectKey {

	int version();	
	
	/**
	 * Returns the key information as a string to be used e.g. for logging   
	 */
	default String toKeyString() {
		return alias() + "[" + id() + "](" + version() + ")";
	}
	
	public static PersistentObjectKey of(String alias, long id, int version) {
		Objects.requireNonNull(alias);
		
		return new PersistentObjectVersionKey() {

			@Override
			public String alias() {
				return alias;
			}

			@Override
			public Long id() {
				return id;
			}

			@Override
			public int version() {
				return version;
			}
			
		};
	}	
	
}
