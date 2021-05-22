package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.base.AbstractPersistentObject.State;
import de.protubero.beanstore.init.BeanStoreFactory;

public class BeanStoreTest {

	@Test
	public void test() throws InterruptedException, ExecutionException {
		var factory = BeanStoreFactory.createNonPersisted();
		factory.registerType(Employee.class);
		var store = factory.create();
		
		store.executeDeferred(ctx -> {
			var tx = ctx.transaction();
			Employee emp = tx.create(Employee.class);
			
			emp.setFirstName("Erik");
			emp.setLastName("Wikinger");
			emp.setAge(3);
			tx.execute();
		});
		
		assertEquals(1, store.reader().objects(Employee.class).count());
		Employee emp2 = store.reader().objects(Employee.class).findFirst().get();
		
		assertEquals("Erik", emp2.getFirstName());
		assertEquals("Wikinger", emp2.getLastName());
		assertEquals(3, emp2.getAge());

		assertEquals("Erik", emp2.get("firstName"));
		assertEquals("Wikinger", emp2.get("lastName"));
		assertEquals(3, emp2.get("age"));
		
		assertNotNull(emp2.id());
		assertEquals(State.READY, emp2.state());
		
		//emp2.setAge(12);
		
		System.out.println(emp2);
		
	}
}
