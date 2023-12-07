package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.base.entity.MapObject;
import de.protubero.beanstore.impl.BeanStoreStateImpl;
import de.protubero.beanstore.model.Employee;
import de.protubero.beanstore.store.EntityStore;
import de.protubero.beanstore.store.Store;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;

public class WriterTest {

	
	
	@Test
	public void happyPathMapStore()  {		
		Store store = new Store();
		StoreWriter storeWriter = new StoreWriter(store);

		var storeReader = new BeanStoreStateImpl(store);
		var employeeStore = store.createMapStore("employee");

				
		assertThrows(RuntimeException.class, () -> {
			@SuppressWarnings("unused")
			EntityStore<MapObject> myStore = store.store("unknown");			
		});
		
		var tx = Transaction.of(store);

		assertThrows(RuntimeException.class, () -> {
			tx.create("x");
		});
		
		var employee1 = tx.create("employee");
		employee1.put("firstName", "John");
		employee1.put("lastName", "Lennon");		
		employee1.put("age", 44);		

		var employee2 = tx.create("employee");
		employee2.put("firstName", "Paul");
		employee2.put("lastName", "McCartney");		
		employee2.put("age", 46);
						
		storeWriter.execute(tx);
				
		assertEquals(2, employeeStore.size());
		
		List<MapObject> employees = employeeStore.objects().sorted().collect(Collectors.toList());
		assertEquals(2, employees.size());
		assertEquals(44, employees.get(0).get("age"));
		assertEquals(46, employees.get(1).get("age"));
		
		assertEquals(44, storeReader.find(employee1).get("age"));
		assertEquals(46, storeReader.find(employee2).get("age"));

		var tx2 = Transaction.of(store);
		var employee3 = tx2.update(employee1);
		employee3.put("age", 55);
		storeWriter.execute(tx2);
		
		assertEquals(55, storeReader.find(employee1).get("age"));
		
		var tx3 = Transaction.of(store);
		tx3.delete(employee3.alias(), employee3.id());
		storeWriter.execute(tx3);
		
		assertFalse(storeReader.findOptional(employee3).isPresent());		
		assertTrue(storeReader.findOptional(employee2).isPresent());		
		
	}
	
	@Test
	public void happyPathBeanStore()  {		
		Store store = new Store();
		StoreWriter storeWriter = new StoreWriter(store);
		var storeReader = new BeanStoreStateImpl(store);
		
		var employeeStore = store.createBeanStore(Employee.class);

						
		var tx = Transaction.of(store);

		
		var employee1 = tx.create(Employee.class);
		employee1.setFirstName("John");
		employee1.setLastName("Lennon");		
		employee1.setAge(44);		
		
		var employee2 = tx.create(Employee.class);
		employee2.setFirstName("Paul");
		employee2.setLastName("McCartney");		
		employee2.setAge(46);
						
		storeWriter.execute(tx);
				
		assertEquals(2, employeeStore.size());
		
		List<Employee> employees = employeeStore.objects().sorted().collect(Collectors.toList());
		assertEquals(2, employees.size());
		assertEquals(44, employees.get(0).getAge());
		assertEquals(46, employees.get(1).getAge());
		
		assertEquals(44, storeReader.find(employee1).getAge());
		assertEquals(46, storeReader.find(employee2).getAge());

		var tx2 = Transaction.of(store);
		Employee employee3 = tx2.update(employee1);
		employee3.setAge(55);
		storeWriter.execute(tx2);
		
		assertEquals(55, storeReader.find(employee1).get("age"));
		
		var tx3 = Transaction.of(store);
		tx3.delete(employee3.alias(), employee3.id());
		storeWriter.execute(tx3);
		
		assertFalse(storeReader.findOptional(employee3).isPresent());		
		assertTrue(storeReader.findOptional(employee2).isPresent());		
		
		var tx4 = Transaction.of(store);
		tx4.delete(employee2);
		storeWriter.execute(tx4);
		
		assertEquals(0, employeeStore.size());
	}
	
}
