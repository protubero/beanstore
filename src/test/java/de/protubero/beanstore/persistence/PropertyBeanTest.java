package de.protubero.beanstore.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.model.Address;
import de.protubero.beanstore.model.PostCode;
import de.protubero.beanstore.model.PostCode2;

public class PropertyBeanTest {

	@TempDir
	File pFileDir;	
	
	
	@Test
	public void test() throws InterruptedException, ExecutionException {
		File file = new File(pFileDir, getClass().getSimpleName() + ".kryo");
		BeanStoreBuilder builder = BeanStoreBuilder.init(file);
		builder.registerEntity(Address.class);
		builder.registerKryoPropertyBean(PostCode.class);
		var store = builder.build();
		
		var address = new Address();
		address.setCity("Berlin");
		address.setStreet("Main Street");
		address.setPostCode(new PostCode("1234"));
		
		var tx = store.transaction();
		tx.create(address);
		tx.execute();
		store.close();
		
		builder = BeanStoreBuilder.init(file);		
		builder.registerEntity(Address.class);
		builder.registerKryoPropertyBean(PostCode.class);
		store = builder.build();
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
