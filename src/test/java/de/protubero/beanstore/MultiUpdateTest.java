package de.protubero.beanstore;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStoreTransactionResult;
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
		tx.update(Keys.key(toni)).setAge(101);
		tx.update(Keys.key(toni)).setLastName("Tuareg");
		BeanStoreTransactionResult result = tx.execute();
		// assertEquals(1, result.getInstanceEvents().size());
	}
	
}
