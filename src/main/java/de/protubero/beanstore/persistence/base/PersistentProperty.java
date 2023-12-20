package de.protubero.beanstore.persistence.base;

import de.protubero.beanstore.base.tx.InstancePropertyValue;

public class PersistentProperty implements InstancePropertyValue {

	private String property;
	private Object value;
	
	@Override
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	
	@Override
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	} 
	
	public static PersistentProperty of(String property, Object value) {
		PersistentProperty ppu = new PersistentProperty();
		ppu.setProperty(property);
		ppu.setValue(value);
		
		return ppu;
	}
	
	@Override
	public String toString() {
		return property + ": " + String.valueOf(value);
	}
}
