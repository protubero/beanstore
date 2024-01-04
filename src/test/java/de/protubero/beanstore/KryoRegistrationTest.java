package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.model.KryoTestEntity;
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;

public class KryoRegistrationTest {
	
	@TempDir
	File pFileDir;	
	
	@Test
	public void test() {
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(pFileDir, "beanstore.kryo"), KryoConfiguration.create()));
		builder.registerEntity(KryoTestEntity.class);
		var store = builder.build();
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
		
		BeanStoreBuilder readBuilder = BeanStoreBuilder.init(KryoPersistence.of(new File(pFileDir, "beanstore.kryo"), KryoConfiguration.create()));
		readBuilder.registerEntity(KryoTestEntity.class);
		var readStore = readBuilder.build();

		var kryoStore = readStore.snapshot().entity(KryoTestEntity.class);
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
