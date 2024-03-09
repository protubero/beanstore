package de.protubero.beanstore.tx;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.AbstractBeanStoreTest;
import de.protubero.beanstore.entity.Keys;
import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.model.Employee;

public class OptimisticLockingTest extends AbstractBeanStoreTest {

	@TempDir
	File pFileDir;
	
	@Test
	public void testFindMethods() throws InterruptedException, ExecutionException {
		var store = addSampleData(createEmptyStore());
		
		var readStore = store.snapshot();

		// data has been correctly stored
		Employee emp1 =  readStore.find(PersistentObjectKey.of(Employee.class, 1));
		
		var tx1 = store.transaction();
		var tx2 = store.transaction();
		
		var empUpd1 = tx1.update(Keys.key(emp1));
		var empUpd2 = tx2.update(Keys.versionKey(emp1));
		
		empUpd1.setAge(13);
		empUpd2.setEmployeeNumber(14);
		
		tx1.execute();
		TransactionFailure failure = assertThrows(TransactionFailure.class, () -> tx2.execute());
		assertSame(TransactionFailureType.OPTIMISTIC_LOCKING_FAILED, failure.getType());
	}

	@Override
	protected File getFileDir() {
		return pFileDir;
	}	

	
}
