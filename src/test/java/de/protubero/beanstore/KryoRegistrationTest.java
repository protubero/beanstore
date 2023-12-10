package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStoreFactory;

public class KryoRegistrationTest {
	
	@TempDir
	File pFileDir;	
	
	@Test
	public void test() {
		BeanStoreFactory factory = BeanStoreFactory.of(new File(pFileDir, "beanstore.kryo"));
		factory.registerEntity(KryoTestEntity.class);
		var store = factory.create();
		var tx = store.transaction();
		var newObj = tx.create(KryoTestEntity.class);
		
		Instant now = Instant.now();
		
		newObj.setLongValue(20l);
		newObj.setIntValue(44);
		newObj.setByteValue(Byte.MAX_VALUE);
		newObj.setFloatValue(3.4f);
		newObj.setShortValue(Short.MAX_VALUE);
		newObj.setDoubleValue(5.6d);
		newObj.setBooleanValue(false);
		newObj.setCharValue('D');
		newObj.setStringValue("AnyText");
		newObj.setInstantValue(now);

		tx.execute();
		
		BeanStoreFactory readFactory = BeanStoreFactory.of(new File(pFileDir, "beanstore.kryo"));
		readFactory.registerEntity(KryoTestEntity.class);
		var readStore = readFactory.create();

		var kryoStore = readStore.state().entity(KryoTestEntity.class);
		assertEquals(1, kryoStore.count());
		var readObj = kryoStore.stream().findFirst().get();
		
		assertEquals(newObj.getLongValue(), readObj.getLongValue());
		assertEquals(newObj.getIntValue(), readObj.getIntValue());
		assertEquals(newObj.getByteValue(), readObj.getByteValue());
		assertEquals(newObj.getFloatValue(), readObj.getFloatValue());
		assertEquals(newObj.getShortValue(), readObj.getShortValue());
		assertEquals(newObj.getDoubleValue(), readObj.getDoubleValue());
		assertEquals(newObj.getBooleanValue(), readObj.getBooleanValue());
		assertEquals(newObj.getCharValue(), readObj.getCharValue());
		assertEquals(newObj.getStringValue(), readObj.getStringValue());
		assertEquals(newObj.getInstantValue(), readObj.getInstantValue());
		
	}
}
