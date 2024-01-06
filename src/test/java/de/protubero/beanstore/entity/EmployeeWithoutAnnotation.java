package de.protubero.beanstore.entity;

public class EmployeeWithoutAnnotation extends AbstractEntity {

	private Integer value;

	public void setValue(Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}
	
}
