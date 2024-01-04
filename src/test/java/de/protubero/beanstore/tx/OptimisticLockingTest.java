package de.protubero.beanstore.tx;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.AbstractBeanStoreTest;
import de.protubero.beanstore.entity.InstanceKey;
import de.protubero.beanstore.model.Employee;
import de.protubero.beanstore.tx.TransactionFailure;
import de.protubero.beanstore.tx.TransactionFailureType;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

public class OptimisticLockingTest extends AbstractBeanStoreTest {

	@TempDir
	File pFileDir;
	
	@Test
	public void testFindMethods() throws InterruptedException, ExecutionException {
		var store = addSampleData(createEmptyStore());
		
		var readStore = store.snapshot();

		// data has been correctly stored
		Employee emp1 = readStore.find(InstanceKey.of("employee", 1));
		
		var tx1 = store.transaction();
		var tx2 = store.transaction();
		
		var empUpd1 = tx1.update(emp1);
		var empUpd2 = tx2.updateOptLocked(emp1);
		
		empUpd1.setAge(13);
		empUpd2.setEmployeeNumber(14);
		
		tx1.execute();
		ExecutionException failure = assertThrows(ExecutionException.class, () -> tx2.executeAsync().get());
		assertSame(TransactionFailureType.OPTIMISTIC_LOCKING_FAILED, ((TransactionFailure) failure.getCause()).getType());
	}

	@Override
	protected File getFileDir() {
		return pFileDir;
	}	

	
}
