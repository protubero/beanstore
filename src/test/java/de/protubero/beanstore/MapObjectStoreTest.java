package de.protubero.beanstore;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStoreFactory;

public class MapObjectStoreTest {

	
	@TempDir
	File pFileDir;	
	
	@Test
	public void test() {
		BeanStoreFactory factory = BeanStoreFactory.of(new File(pFileDir, getClass().getSimpleName() + ".kryo"));
		
		
		
	}	
	
}
