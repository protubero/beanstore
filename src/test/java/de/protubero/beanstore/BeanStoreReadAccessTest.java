package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreException;
import de.protubero.beanstore.entity.InstanceKey;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.model.Employee;

public class BeanStoreReadAccessTest extends AbstractBeanStoreTest {

	@TempDir
	File pFileDir;
	
	@Test
	public void testFindMethods() throws InterruptedException, ExecutionException {
		var store = addSampleData(createEmptyStore());
		
		var readStore = store.snapshot();

		// data has been correctly stored
		Employee emp1 = readStore.find(InstanceKey.of("employee", 1));
		assertEqualsSampleData(emp1);
		
		Employee emp2 = readStore.find(emp1);
		assertSame(emp1, emp2);
		
		assertThrows(NullPointerException.class, () -> readStore.find((InstanceKey) null));
		assertThrows(NullPointerException.class, () -> readStore.find((AbstractPersistentObject) null));
		
		assertThrows(BeanStoreException.class, () -> readStore.find(instanceKey(null, 1l)));
		assertThrows(BeanStoreException.class, () -> readStore.find(instanceKey("employee", null)));
		assertThrows(BeanStoreException.class, () -> readStore.find(SAMPLE_DATA[0]));

		var tx =store.transaction();
		tx.update(emp1).setAge(121);
		tx.execute().get();
		
		
		
		var updatedObj = store.snapshot().find(emp1);
		
		assertEquals(121, updatedObj.getAge());
		assertEquals(updatedObj.getFirstName(), emp1.getFirstName());
		assertEquals(updatedObj.getLastName(), emp1.getLastName());
		assertEquals(updatedObj.id(), emp1.id());
		assertEquals(updatedObj.alias(), emp1.alias());
		
		assertEquals(State.OUTDATED, emp1.state());
		assertEquals(State.STORED, updatedObj.state());
	}

	@Test
	public void testFindOptional()  {
		var store = addSampleData(createEmptyStore());
		
		var readStore = store.snapshot();
		assertThrows(NullPointerException.class, () -> readStore.findOptional(null));
		
		assertThrows(BeanStoreException.class, () -> readStore.findOptional(instanceKey(null, 1l)));		
		assertThrows(BeanStoreException.class, () -> readStore.findOptional(instanceKey("employee", null)));		
		assertThrows(BeanStoreException.class, () -> readStore.findOptional(SAMPLE_DATA[0]));
		
		Employee emp1 = readStore.find(InstanceKey.of("employee", 1));
		assertEquals(true, readStore.findOptional(emp1).isPresent());
		assertEquals(false, readStore.findOptional(instanceKey("employee", -1000l)).isPresent());
	}

	@Override
	protected File getFileDir() {
		return pFileDir;
	}

	
}
