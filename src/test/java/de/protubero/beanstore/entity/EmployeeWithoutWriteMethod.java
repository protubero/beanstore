package de.protubero.beanstore.entity;

@Entity(alias = "employee")
public class EmployeeWithoutWriteMethod extends AbstractEntity {

	private Integer value;

	public Integer getValue() {
		return value;
	}



	
}
