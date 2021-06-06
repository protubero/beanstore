package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.base.tx.TransactionFailure;
import de.protubero.beanstore.model.Employee;

public class TransactionExceptionTest extends AbstractBeanStoreTest {

	
	@Override
	protected File getFileDir() {
		return pFileDir;
	}
	
	@TempDir
	File pFileDir;
	
	
	
	@Test
	public void test() {
		var store = addSampleData(createEmptyStore());
		var readAccess = store.read().entity(Employee.class);
		
		var toni = readAccess.stream().filter(obj -> obj.getEmployeeNumber() == 1).findAny().get();
		
		// change it -> creates a new version
		var tx = store.transaction();
		tx.update(toni).setAge(101);
		
		// change it again -> provoke optimistic locking failure
		var tx2 = store.transaction();
		tx2.update(toni).setAge(102);

		var tx3 = store.transaction();
		tx3.update(toni).setAge(104);
		
		
		tx.execute();
		assertThrows(TransactionFailure.class, () -> { tx2.execute();});
//		tx3.executeAsync(te -> {
//			assertTrue(te.failed());
//			assertTrue(te.exception() != null);
//		});
		
	}
	
}
