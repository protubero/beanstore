package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.InstanceKey;
import de.protubero.beanstore.model.Address;
import de.protubero.beanstore.model.Employee;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;

public abstract class AbstractBeanStoreTest {

	
	public static AtomicInteger beanStoreId = new AtomicInteger(); 
	
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
		BeanStoreBuilder builder = createEmptyStoreBuilder(plugin);
		builder.registerEntity(Employee.class);
		builder.registerEntity(Address.class);
		return builder.build();
	}

	protected BeanStoreBuilder createEmptyStoreBuilder() throws AssertionError {
		return createEmptyStoreBuilder(null);
	}

	
	protected BeanStoreBuilder createEmptyStoreBuilder(BeanStorePlugin plugin) throws AssertionError {
		File pFileDir = getFileDir();
		
		if (pFileDir == null) {
			throw new AssertionError();
		}
		File file = new File(pFileDir, "beanstore_" + beanStoreId.addAndGet(1) + ".kryo");
		if (file.exists()) {
			throw new AssertionError();
		}
		BeanStoreBuilder builder = BeanStoreBuilder.init(file);
		if (plugin != null) {
			builder.addPlugin(plugin);
		}
		return builder;
	}
	
	protected abstract File getFileDir();

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
		try {
			tx.execute().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		return store;
	}
	
}
