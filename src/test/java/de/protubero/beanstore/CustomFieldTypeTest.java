package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStoreFactory;
import de.protubero.beanstore.api.BeanStorePlugin;
import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.model.Address;
import de.protubero.beanstore.model.Employee;
import de.protubero.beanstore.model.PostCode;

public class CustomFieldTypeTest {

	@TempDir
	File pFileDir;	
	
	
	@Test
	public void test() {
		File file = new File(pFileDir, "beanstore_" + getClass().getSimpleName() + ".kryo");
		BeanStoreFactory factory = BeanStoreFactory.of(file);
		factory.registerEntity(Address.class);
		var store = factory.create();
		
		var address = new Address();
		address.setCity("Berlin");
		address.setStreet("Main Street");
		address.setPostCode(new PostCode("1234"));
		
		var tx = store.transaction();
		tx.create(address);
		tx.execute();
		store.close();
		
		factory = BeanStoreFactory.of(file);		
		factory.registerEntity(Address.class);
		store = factory.create();
		var readAccess = store.state().entity(Address.class);
		
		var addressList = readAccess.asList();
		assertEquals(1, addressList.size());
		var addr = addressList.get(0);
		assertEquals("Berlin", addr.getCity());
		assertEquals("Main Street", addr.getStreet());
		
		var postCode = addr.getPostCode();
		assertEquals("1234", postCode.getCode());		
	}	

}
