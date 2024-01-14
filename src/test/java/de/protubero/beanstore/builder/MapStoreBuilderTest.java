package de.protubero.beanstore.builder;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.entity.MapObject;
import de.protubero.beanstore.model.Employee;
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;

public class MapStoreBuilderTest {

	@Test
	public void testMapStoreBuilder(@TempDir File tempDir) {
		File tempFile = new File(tempDir, getClass().getSimpleName() +  ".kry");
		
		KryoPersistence persistence = KryoPersistence.of(tempFile, KryoConfiguration.create());
		
		var builder = BeanStoreBuilder.init(persistence);
		builder.registerEntity(Employee.class);
		var store = builder.build();

		var tx = store.transaction();
		var emp = tx.create(Employee.class);
		emp.setFirstName("Walter");
		emp.setLastName("Ulbricht");
		emp.setAge(33);
		tx.execute();
		store.close();
		
		
		var mapStore = MapStoreBuilder.init(persistence.clonePersistence()).build();
		var es = mapStore.snapshot().mapEntity("employee");
		Assertions.assertEquals(1, es.count());
		
		MapObject employee = es.find(0);
		
		Assertions.assertEquals(3, employee.size());
		Assertions.assertEquals(33, employee.get("age"));
		Assertions.assertEquals("Walter", employee.get("firstName"));
		Assertions.assertEquals("Ulbricht", employee.get("lastName"));
		
		tx = mapStore.transaction();

		MapObject exEmp = tx.update(employee);
		exEmp.set("age", 50);
		
		employee = (MapObject) tx.create("employee");
		employee.set("firstName", "John");
		employee.set("lastName", "Wayne");
		employee.set("age", 44);
		
		
		tx.execute();
		mapStore.close();
		
		builder = BeanStoreBuilder.init(persistence.clonePersistence());
		builder.registerEntity(Employee.class);
		store = builder.build();
		
		var empStore = store.snapshot().entity(Employee.class);
		Assertions.assertEquals(2, empStore.count());
		var walter = empStore.find(0);
		Assertions.assertEquals(50, walter.getAge());
		var john = empStore.find(1);		
		Assertions.assertEquals(44, john.getAge());
	}
	
}
