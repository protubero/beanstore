package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.model.Employee;

public class BeanStoreTest {

	@Test
	public void test() throws InterruptedException, ExecutionException {
		var builder = BeanStoreBuilder.init();
		builder.registerEntity(Employee.class);
		var store = builder.build();
	
		var tx = store.transaction();
		Employee emp = tx.create(Employee.class);
		
		emp.setFirstName("Erik");
		emp.setLastName("Wikinger");
		emp.setAge(3);
		tx.execute().get();
		
		assertEquals(1, store.state().entity(Employee.class).count());
		Employee emp2 = store.state().entity(Employee.class).stream().findFirst().get();
		
		assertEquals("Erik", emp2.getFirstName());
		assertEquals("Wikinger", emp2.getLastName());
		assertEquals(3, emp2.getAge());

		assertEquals("Erik", emp2.get("firstName"));
		assertEquals("Wikinger", emp2.get("lastName"));
		assertEquals(3, emp2.get("age"));
		
		assertNotNull(emp2.id());
		assertEquals(State.STORED, emp2.state());
		
		//emp2.setAge(12);
		
		System.out.println(emp2);
		
	}
}
