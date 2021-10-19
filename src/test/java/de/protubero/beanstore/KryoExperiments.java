package de.protubero.beanstore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

class KryoExperiments {

	public static class Employee {
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

	@Test
	void test() throws FileNotFoundException {
		
//		Kryo kryo = new Kryo();
//		kryo.
//		kryo.register(Employee.class);
//
//		Employee object = new Employee();
//		object.setAge(33);
//		object.setEmployeeNumber(3);
//		object.setFirstName("Walt");
//		object.setLastName("Disney");
//
//		Output output = new Output(new FileOutputStream("c:/work/file.bin"));
//		kryo.writeObject(output, object);
//		output.close();
//
//		Input input = new Input(new FileInputStream("c:/work/file.bin"));
//		Employee object2 = kryo.readObject(input, Employee.class);
//		input.close();
	}

}
