package de.protubero.beanstore.benchmark;

import java.io.File;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.factory.BeanStoreFactory;
import de.protubero.beanstore.model.Employee;

public class BenchmarkTestTool {

	private static final int NUM_OBJECTS = 1000;

	@Test
	public void name(@TempDir File tempDir) {
		var file = new File(tempDir, getClass().getSimpleName() + ".kryo");
		BeanStoreFactory builder = BeanStoreFactory.init(file);
		builder.registerEntity(Employee.class);
		BeanStore beanStore = builder.create();

		System.out.println("Num Objects: " + NUM_OBJECTS);
		
		var startGenMillis = System.currentTimeMillis();
		for (int i = 0; i < NUM_OBJECTS; i++) {
			var tx = beanStore.transaction();
			
			var employee1 = tx.create(Employee.class);
			employee1.setFirstName("John");
			employee1.setLastName("Lennon");		
			employee1.setAge(44);		
			tx.executeBlocking();
		}
		var stopGenMillis = System.currentTimeMillis();
		System.out.println("Data Generation Time [ms] : " + (stopGenMillis - startGenMillis));
		
		var startIterMillis = System.currentTimeMillis();

		var resultList = beanStore.state().entity(Employee.class).stream()
		.filter(emp -> emp.getAge() == 44) 
		.filter(emp -> emp.getFirstName().equals("John")) 
		.filter(emp -> emp.getLastName().equals("Lennon") 
		).collect(Collectors.toList());
		Assertions.assertEquals(NUM_OBJECTS, resultList.size());

		var stopIterMillis = System.currentTimeMillis();
		System.out.println("Iteration Time [ms] : " + (stopIterMillis - startIterMillis));

		var startAccessMillis = System.currentTimeMillis();
		var es = beanStore.state().entity(Employee.class);
		for (long i = 0; i < NUM_OBJECTS; i++) {
			Assertions.assertNotNull(es.find(i));
		}
		var stopAccessMillis = System.currentTimeMillis();
		System.out.println("Access Time [ms] : " + (stopAccessMillis - startAccessMillis));
		
		
		System.out.println("File length: " + file.length());
		
		var startLoadMillis = System.currentTimeMillis();
		BeanStoreFactory builder2 = BeanStoreFactory.init(file);
		builder2.registerEntity(Employee.class);
		BeanStore beanStore2 = builder2.create();
		var stopLoadMillis = System.currentTimeMillis();
		System.out.println("Load Time [ms] : " + (stopLoadMillis - startLoadMillis));
		
		int numLoaded = beanStore2.state().entity(Employee.class).count();
		Assertions.assertEquals(NUM_OBJECTS, numLoaded);
		System.out.println("Num loaded: " + numLoaded);
	}
	
	
	
}
