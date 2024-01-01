package de.protubero.beanstore.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCompanion<T extends AbstractPersistentObject> implements Companion<T> {

	public static final Logger log = LoggerFactory.getLogger(AbstractCompanion.class);
	
	
	@Override
	public String toString(T instance) {		
		StringBuilder result = new StringBuilder(alias() + " [" + instance.id() + "]");
		
		instance.forEach((key, value) -> {
			result.append(System.lineSeparator() + " " + key + "=" + String.valueOf(value));
		});
		
		return result.toString();
	}
	
	@Override
	public String toString() {
		return "entity " + alias() + " [" + entityClass().getSimpleName() + "]";
	}
	
}
