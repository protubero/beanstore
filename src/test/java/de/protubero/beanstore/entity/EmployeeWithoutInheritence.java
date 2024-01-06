package de.protubero.beanstore.entity;

@Entity(alias = "employee")
public class EmployeeWithoutInheritence {

	private Integer value;

	public void setValue(Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}
	
}
