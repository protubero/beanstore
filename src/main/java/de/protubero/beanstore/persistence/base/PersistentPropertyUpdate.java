package de.protubero.beanstore.persistence.base;

import de.protubero.beanstore.base.BeanPropertyChange;

public class PersistentPropertyUpdate implements BeanPropertyChange {

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
	
	public static PersistentPropertyUpdate of(String property, Object value) {
		PersistentPropertyUpdate ppu = new PersistentPropertyUpdate();
		ppu.setProperty(property);
		ppu.setValue(value);
		
		return ppu;
	}
}
