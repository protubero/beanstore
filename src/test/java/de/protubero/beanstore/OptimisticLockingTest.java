package de.protubero.beanstore;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.base.entity.InstanceKey;
import de.protubero.beanstore.base.tx.TransactionFailure;
import de.protubero.beanstore.base.tx.TransactionFailureType;
import de.protubero.beanstore.model.Employee;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

public class OptimisticLockingTest extends AbstractBeanStoreTest {

	@TempDir
	File pFileDir;
	
	@Test
	public void testFindMethods() {
		var store = addSampleData(createEmptyStore());
		
		var readStore = store.state();

		// data has been correctly stored
		Employee emp1 = readStore.find(InstanceKey.of("employee", 1));
		
		var tx1 = store.transaction();
		var tx2 = store.transaction();
		
		var empUpd1 = tx1.update(emp1);
		var empUpd2 = tx2.update(emp1);
		
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
