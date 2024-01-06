package de.protubero.beanstore.builder;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
		
		
		/*
		var mapStore = MapStoreBuilder.init(persistence).build();
		
		var es = mapStore.snapshot().entity("employee");
		Assertions.assertEquals(1, es.count());
		*/
	}
	
}
