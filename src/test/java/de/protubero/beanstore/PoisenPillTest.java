package de.protubero.beanstore;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.factory.BeanStoreFactory;
import de.protubero.beanstore.model.Employee;

class PoisenPillTest {

//	@Test
//	void test() {
//		var factory = BeanStoreFactory.of(new File("c:/work/pilltest.kryo"));
//		factory.registerEntity(Employee.class);
//		var store = factory.create();
//		
//		System.out.println("count = " + store.state().entity(Employee.class).count());
//		
//		for (int i = 0; i < 10; i++) {
//			var tx = store.transaction();
//			Employee emp = tx.create(Employee.class);
//			
//			emp.setFirstName("Erik");
//			emp.setLastName("Wikinger");
//			emp.setAge(3);
//			tx.execute();
//		}	
//		
//		System.out.println("#####################################");
//		// store.close();
//		
//		System.out.println("#####################################");
//	}

}
