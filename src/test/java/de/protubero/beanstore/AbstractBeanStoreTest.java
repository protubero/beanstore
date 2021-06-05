package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreFactory;
import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.InstanceKey;
import de.protubero.beanstore.model.Employee;

public abstract class AbstractBeanStoreTest {

	@TempDir
	File pFileDir;
	
	static Employee[] SAMPLE_DATA = new Employee[] {
		new Employee("Werner", "Liebrich", 27),	
		new Employee("Horst", "Eckel", 22),	
		new Employee("Fritz", "Walter", 34),	
		new Employee("Helmut", "Rahn", 25),	
		new Employee("Ottmar", "Walter", 30),	
		new Employee("Max", "Morlock", 29),	
		new Employee("Toni", "Tureck", 35)	
	};
	
	protected BeanStore createEmptyStore() {
		BeanStoreFactory factory = BeanStoreFactory.of(new File(pFileDir, "beanstore.kryo"));
		factory.registerEntity(Employee.class);
		return factory.create();
	}
	
	protected InstanceKey instanceKey(String alias, Long id) {
		return new InstanceKey() {
			
			@Override
			public Long id() {
				return id;
			}
			
			@Override
			public String alias() {
				return alias;
			}
		};
	}
	
	protected Employee empByLastName(String lastName) {
		for (Employee emp : SAMPLE_DATA) {
			if (emp.getLastName().equals(lastName)) {
				return emp;
			}
		}
		throw new AssertionError("unknown name: " + lastName);
	}
	
	protected void equalsSampleData(Employee emp) {
		assertNotNull(emp, "employee ref must not be null");
		assertNotNull(emp.getLastName(), "employee last name must not be null");
		
		var sampleEmployee = empByLastName(emp.getLastName());
		assertEquals(sampleEmployee.getFirstName(), emp.getFirstName());
		assertEquals(sampleEmployee.getAge(), emp.getAge());
	}
	
	protected BeanStore addSampleData(BeanStore store) {
		var tx = store.transaction();
		for (AbstractEntity ae : SAMPLE_DATA) {
			tx.create(ae);
		}
		tx.execute();
		return store;
	}
	
}
