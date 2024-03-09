package de.protubero.beanstore.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.keys.Keys;
import de.protubero.beanstore.model.Employee;
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;
import de.protubero.beanstore.plugins.txlog.BeanStoreTransactionLogPlugin;

public class BeanStoreBasicTest {
	
	@Test
	public void happyPathBeanStore(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreBuilder builder = createBuilder(tempDir);
		BeanStore beanStore = builder.build();
		
		var tx = beanStore.transaction();
		
		var employee1 = tx.create(Employee.class);
		employee1.setFirstName("John");
		employee1.setLastName("Lennon");		
		employee1.setAge(44);		
		
		var employee2 = tx.create(Employee.class);
		employee2.setFirstName("Paul");
		employee2.setLastName("McCartney");		
		employee2.setAge(46);
						
		tx.executeAsync();
		
		beanStore.close();

		assertEquals(2, beanStore.snapshot().entity(Employee.class).count());
		
		builder = createBuilder(tempDir);
		builder.addMigration("eins", mTx -> {
			mTx.state()
			.mapEntity("employee")
			.stream()
			.filter(emp -> emp.get("firstName").equals("Paul"))
			.forEach(e -> {
				var update = mTx.update(e);
				update.put("age", e.getInteger("age") + 3);
			});
		});
		var beanStore2 = builder.build();
				
		assertEquals(2, beanStore2.snapshot().entity(Employee.class).count());
		
		List<Employee> employees = beanStore2.snapshot().entity(Employee.class).stream().sorted().collect(Collectors.toList());
		assertEquals(2, employees.size());
		assertEquals(44, employees.get(0).getAge());
		assertEquals(49, employees.get(1).getAge());
		
		assertEquals(44, beanStore2.snapshot().find(Keys.key(employee1)).getAge());
		assertEquals("John", beanStore2.snapshot().find(Keys.key(employee1)).getFirstName());
		assertEquals(49, beanStore2.snapshot().find(Keys.key(employee2)).getAge());
	}

	private BeanStoreBuilder createBuilder(File tempDir) {
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder.registerEntity(Employee.class);
		builder.addPlugin(new BeanStoreTransactionLogPlugin());
		return builder;
	}
	
}
