package de.protubero.beanstore;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;

public class MapObjectStoreTest {

	
	@TempDir
	File pFileDir;	
	
	@Test
	public void test() {
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(pFileDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		
		
		
	}	
	
}
