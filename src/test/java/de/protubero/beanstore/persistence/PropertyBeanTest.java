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
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;

public class PropertyBeanTest {

	@TempDir
	File pFileDir;	
	
	
	@Test
	public void test() throws InterruptedException, ExecutionException {
		File file = new File(pFileDir, getClass().getSimpleName() + ".kryo");
		var kryoConfig = KryoConfiguration.create();
		kryoConfig.register(PostCode.class);
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(file, kryoConfig));
		builder.registerEntity(Address.class);
		var store = builder.build();
		
		var address = new Address();
		address.setCity("Berlin");
		address.setStreet("Main Street");
		address.setPostCode(new PostCode("1234"));
		
		var tx = store.transaction();
		tx.create(address);
		tx.executeAsync();
		store.close();
		
		builder = BeanStoreBuilder.init(KryoPersistence.of(file, kryoConfig));
		builder.registerEntity(Address.class);
		store = builder.build();
		var readAccess = store.snapshot().entity(Address.class);
		
		var addressList = readAccess.asList();
		assertEquals(1, addressList.size());
		var addr = addressList.get(0);
		assertEquals("Berlin", addr.getCity());
		assertEquals("Main Street", addr.getStreet());
		
		var postCode = addr.getPostCode();
		assertEquals("1234", postCode.getCode());		
	}	

}
