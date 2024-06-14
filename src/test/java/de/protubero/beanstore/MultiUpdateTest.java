package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.entity.Keys;
import de.protubero.beanstore.model.Employee;

public class MultiUpdateTest extends AbstractBeanStoreTest {

	@Override
	protected File getFileDir() {
		return pFileDir;
	}

	@TempDir
	File pFileDir;
	

	
	@Test
	public void test() throws InterruptedException, ExecutionException {
		var store = addSampleData(createEmptyStore());
		var readAccess = store.snapshot().entity(Employee.class);
		
		var toni = readAccess.stream().filter(obj -> obj.getEmployeeNumber() == 1).findAny().get();
		var tx = store.transaction();
		var firstRec = tx.update(Keys.key(toni));
		firstRec.setAge(101);
		var secondRec = tx.update(Keys.key(toni));
		assertSame(firstRec, secondRec);
		secondRec.setLastName("Tuareg");
		tx.execute();
		
		var toni2 = store.get(toni);
		assertEquals(101, toni2.getAge().intValue());
		assertEquals("Tuareg", toni2.getLastName());
		
//		assertThrows(Exception.class, null) BeanStoreTransactionResult result = tx.execute();
	}
	
}
