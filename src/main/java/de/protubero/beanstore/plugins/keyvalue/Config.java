package de.protubero.beanstore.plugins.keyvalue;

public interface Config {

	Object get(String path);
		
	Config subConfig(String path);
	
	boolean exists();
	
	
}
