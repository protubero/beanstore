package de.protubero.beanstore.entity;

@Entity(alias = "employee")
public class EmployeeWithDefaultValue extends AbstractEntity {

	private Integer value = 1;

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	
}
