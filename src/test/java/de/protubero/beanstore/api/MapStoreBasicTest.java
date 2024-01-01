package de.protubero.beanstore.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.entity.MapObject;
import de.protubero.beanstore.factory.BeanStoreFactory;

public class MapStoreBasicTest {

	
	@Test
	public void happyPathBeanStore(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreFactory builder = createBuilder(tempDir);
		BeanStore beanStore = builder.create();
		
		var tx = beanStore.transaction();
		
		var employee1 = tx.create("employee");
		employee1.put("firstName", "John");
		employee1.put("lastName", "Lennon");		
		employee1.put("age", 44);		
		
		var employee2 = tx.create("employee");
		employee2.put("firstName", "Paul");
		employee2.put("lastName", "McCartney");		
		employee2.put("age", 46);		
						
		tx.execute();
		
		beanStore.close();

		assertEquals(2, beanStore.state().entity("employee").count());
		
		builder = createBuilder(tempDir);
		builder.addMigration("eins", mTx -> {
			mTx.state()
			.mapEntity("employee")
			.stream()
			.filter(emp -> {
				return emp.get("firstName").equals("Paul");
			})
			.forEach(e -> {
				var update = mTx.update(e);
				update.put("age", e.getInteger("age") + 3);
			});
		});
		var beanStore2 = builder.create();
				
		assertEquals(2, beanStore2.state().entity("employee").count());
		
		List<MapObject> employees = beanStore2.state().mapEntity("employee").stream().sorted().collect(Collectors.toList());
		assertEquals(2, employees.size());
		assertEquals(44, employees.get(0).get("age"));
		assertEquals(49, employees.get(1).get("age"));
		
		assertEquals(44, beanStore2.state().find(employee1).get("age"));
		assertEquals("John", beanStore2.state().find(employee1).get("firstName"));
		assertEquals(49, beanStore2.state().find(employee2).get("age"));
	}

	private BeanStoreFactory createBuilder(File tempDir) {
		BeanStoreFactory builder = BeanStoreFactory.of(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		builder.registerMapEntity("employee");
		return builder;
	}
	
	
}
