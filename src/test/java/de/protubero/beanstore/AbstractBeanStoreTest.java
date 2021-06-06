package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreFactory;
import de.protubero.beanstore.api.BeanStorePlugin;
import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.InstanceKey;
import de.protubero.beanstore.model.Employee;

public abstract class AbstractBeanStoreTest {

	@TempDir
	File pFileDir;
	
	static Employee[] SAMPLE_DATA = new Employee[] {
		new Employee(10, "Werner", "Liebrich", 27),	
		new Employee(6, "Horst", "Eckel", 22),	
		new Employee(16, "Fritz", "Walter", 34),	
		new Employee(12, "Helmut", "Rahn", 25),	
		new Employee(15, "Ottmar", "Walter", 30),	
		new Employee(13, "Max", "Morlock", 29),	
		new Employee(1, "Toni", "Tureck", 35)	
	};

	protected BeanStore createEmptyStore() {
		return createEmptyStore(null);
	}
	
	protected BeanStore createEmptyStore(BeanStorePlugin plugin) {
		BeanStoreFactory factory = BeanStoreFactory.of(new File(pFileDir, "beanstore.kryo"));
		if (plugin != null) {
			factory.addPlugin(plugin);
		}
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

	protected Employee empByEmployeeNumber(int number) {
		for (Employee emp : SAMPLE_DATA) {
			if (emp.getEmployeeNumber() == number) {
				return emp;
			}
		}
		throw new AssertionError("unknown employee number: " + number);
	}
	
	protected void assertEqualsSampleData(Employee emp) {
		assertNotNull(emp, "employee ref must not be null");
		assertNotNull(emp.getLastName(), "employee last name must not be null");
		
		var sampleEmployee = empByEmployeeNumber(emp.getEmployeeNumber());
		assertEquals(sampleEmployee.getFirstName(), emp.getFirstName());
		assertEquals(sampleEmployee.getLastName(), emp.getLastName());
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
