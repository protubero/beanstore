package de.protubero.beanstore.entity;

@Entity(alias = "employee")
public class EmployeeWithoutReadMethod extends AbstractEntity {

	private Integer value;

	public void setValue(Integer value) {
		this.value = value;
	}
	
}
