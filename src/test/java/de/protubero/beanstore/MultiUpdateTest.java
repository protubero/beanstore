package de.protubero.beanstore;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStoreTransactionResult;
import de.protubero.beanstore.base.tx.TransactionEvent;
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
		var readAccess = store.state().entity(Employee.class);
		
		var toni = readAccess.stream().filter(obj -> obj.getEmployeeNumber() == 1).findAny().get();
		var tx = store.transaction();
		tx.update(toni).setAge(101);
		tx.update(toni).setLastName("Tuareg");
		BeanStoreTransactionResult result = tx.execute().get();
		// assertEquals(1, result.getInstanceEvents().size());
	}
	
}
