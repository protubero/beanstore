package de.protubero.beanstore.entity;

import de.protubero.beanstore.model.Employee;

@Entity(alias = "employee")
public class EmployeeWithInvalidRef extends AbstractEntity {

	private Employee employee = null;

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}


	
}
