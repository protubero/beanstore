package de.protubero.beanstore.migration;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.model.Employee;

public class MigrationTest {

	@Test
	public void invalidMigrationIds(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreBuilder builder = BeanStoreBuilder.init(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		Assertions.assertThrows(Exception.class, () -> { builder.addMigration(" xyz", tx -> {});});
		Assertions.assertThrows(Exception.class, () -> { builder.addMigration("_xyz", tx -> {});});
		builder.addMigration("xyz", tx -> {});
		Assertions.assertThrows(Exception.class, () -> { builder.addMigration("xyz", tx -> {});});
	}	
	
	@Test
	public void multipleMigrations(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreBuilder builder = BeanStoreBuilder.init(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		builder.registerEntity(Employee.class);
		var store = builder.build();
		
		var tx = store.transaction();
		var employee1 = tx.create(Employee.class);
		employee1.setFirstName("John");
		employee1.setLastName("Lennon");		
		employee1.setAge(44);		

		tx.execute();
		store.close();
		
		
		builder = BeanStoreBuilder.init(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		builder.registerEntity(Employee.class);
		
		builder.addMigration("a", mtx -> {
			var obj = mtx.update("employee", 0);
			obj.put("lastName", "Wayne");
			obj.put("age", 45);
		});
		
		
		store = builder.build();
		store.close();
		
		builder = BeanStoreBuilder.init(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		builder.registerEntity(Employee.class);
		
		builder.addMigration("a", mtx -> {
			var obj = mtx.update("employee", 0);
			obj.put("lastName", "x");
			obj.put("age", 1);
		});
		builder.addMigration("b", mtx -> {
			var employee = mtx.state().mapEntity("employee").find(0);
			var obj = mtx.update("employee", 0);
			obj.put("age", ((Integer) employee.get("age")) * 2);
		});
		
		store = builder.build();
		var employee = store.snapshot().entity(Employee.class).find(0);
		System.out.println(employee);
		Assertions.assertEquals("Wayne", employee.get("lastName"));
		Assertions.assertEquals(90, employee.get("age"));
		
		store.close();
	}
}
