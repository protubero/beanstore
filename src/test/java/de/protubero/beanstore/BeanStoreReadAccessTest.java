package de.protubero.beanstore;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.base.entity.InstanceKey;
import de.protubero.beanstore.model.Employee;

public class BeanStoreReadAccessTest extends AbstractBeanStoreTest {

	@Test
	public void test() {
		var store = addSampleData(createEmptyStore());
		
		var readStore = store.read();
		var readEntity = readStore.entity(Employee.class);

		// data has been correctly stored
		Employee emp1 = readStore.find(InstanceKey.of("employee", 1));
		equalsSampleData(emp1);
		
		readStore.find(null);
	}

	
}
