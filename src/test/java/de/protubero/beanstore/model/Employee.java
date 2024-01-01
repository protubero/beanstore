package de.protubero.beanstore.model;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity(alias = "employee")
public class Employee extends AbstractEntity {

	private String firstName;
	private String lastName;
		
	@Min(value = 1, message = "Age should not be less than 18")
    @Max(value = 100, message = "Age should not be greater than 150")	
	private Integer age;

	private Integer employeeNumber;
	
	public Employee() {
	}
	
	public Employee(int employeeNumber, String firstName, String lastName, Integer age) {
		this.employeeNumber = employeeNumber;
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}

	public Integer getEmployeeNumber() {
		return employeeNumber;
	}

	public void setEmployeeNumber(Integer employeeNumber) {
		this.employeeNumber = employeeNumber;
	}
	
}
