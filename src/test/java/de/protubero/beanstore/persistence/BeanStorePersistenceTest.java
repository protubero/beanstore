package de.protubero.beanstore.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreFactory;
import de.protubero.beanstore.model.Employee;

public class BeanStorePersistenceTest {
	
	@Test
	public void happyPathBeanStore(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreFactory builder = createBuilder(tempDir);
		BeanStore beanStore = builder.create();
		
		var tx = beanStore.transaction();
		
		var employee1 = tx.create(Employee.class);
		employee1.setFirstName("John");
		employee1.setLastName("Lennon");		
		employee1.setAge(44);		
		
		var employee2 = tx.create(Employee.class);
		employee2.setFirstName("Paul");
		employee2.setLastName("McCartney");		
		employee2.setAge(46);
						
		tx.execute();
		
		beanStore.close().get();

		assertEquals(2, beanStore.state().entity(Employee.class).count());
		
		builder = createBuilder(tempDir);
		builder.addMigration("eins", mTx -> {
			mTx.state()
			.entity("employee")
			.stream()
			.filter(emp -> emp.get("firstName").equals("Paul"))
			.map(e -> mTx.update(e)).forEach(e -> {
				e.put("age", e.getInteger("age") + 3);
			});
		});
		var beanStore2 = builder.create();
				
		assertEquals(2, beanStore2.state().entity(Employee.class).count());
		
		List<Employee> employees = beanStore2.state().entity(Employee.class).stream().sorted().collect(Collectors.toList());
		assertEquals(2, employees.size());
		assertEquals(44, employees.get(0).getAge());
		assertEquals(49, employees.get(1).getAge());
		
		assertEquals(44, beanStore2.state().find(employee1).getAge());
		assertEquals(49, beanStore2.state().find(employee2).getAge());
	}

	private BeanStoreFactory createBuilder(File tempDir) {
		BeanStoreFactory builder = BeanStoreFactory.of(new File(tempDir, "anyFile.kryo"));
		builder.registerEntity(Employee.class);
		return builder;
	}
	
}
