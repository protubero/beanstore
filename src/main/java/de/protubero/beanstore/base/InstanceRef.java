package de.protubero.beanstore.base;

public interface InstanceRef {

	String alias();
	
	Long id();
	
	default String toRefString() {
		return alias() + "[" + id() + "]";
	}
	
}
