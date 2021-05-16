package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.init.BeanStore;
import de.protubero.beanstore.init.BeanStoreFactory;
import de.protubero.beanstore.store.ReadableBeanStore;

public class BeanStorePersistenceTest {
	
	@Test
	public void happyPathBeanStore(@TempDir File tempDir)  {
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
		
		beanStore.close();
		
		builder = createBuilder(tempDir);
		builder.addMigration("eins", mTx -> {
			mTx.dataStore()
			.objects("employee")
			.filter(emp -> emp.get("firstName").equals("Paul"))
			.map(e -> mTx.update(e)).forEach(e -> {
				e.put("age", e.getInteger("age") + 3);
			});
		});
		ReadableBeanStore beanStore2 = builder.create();
				
		assertEquals(2, beanStore2.objects(Employee.class).count());
		
		List<Employee> employees = beanStore2.objects(Employee.class).sorted().collect(Collectors.toList());
		assertEquals(2, employees.size());
		assertEquals(44, employees.get(0).getAge());
		assertEquals(49, employees.get(1).getAge());
		
		assertEquals(44, beanStore2.find(employee1).getAge());
		assertEquals(49, beanStore2.find(employee2).getAge());

	}

	private BeanStoreFactory createBuilder(File tempDir) {
		BeanStoreFactory builder = BeanStoreFactory.of(new File(tempDir, "anyFile.kryo"));
		builder.registerType(Employee.class);
		return builder;
	}
	
}
