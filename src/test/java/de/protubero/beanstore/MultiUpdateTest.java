package de.protubero.beanstore;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
	public void test() {
		var store = addSampleData(createEmptyStore());
		var readAccess = store.read().entity(Employee.class);
		
		var toni = readAccess.stream().filter(obj -> obj.getEmployeeNumber() == 1).findAny().get();
		var tx = store.transaction();
		tx.update(toni).setAge(101);
		tx.update(toni).setLastName("Tuareg");
		TransactionEvent result = tx.execute();
		// assertEquals(1, result.getInstanceEvents().size());
	}
	
}
