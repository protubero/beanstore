package de.protubero.beanstore;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStoreFactory;
import de.protubero.beanstore.model.LogEntry;
import de.protubero.beanstore.model.LogLevel;

public class EnumTest {

	@TempDir
	File pFileDir;	
	
	@Test
	public void test() {
		File file = new File(pFileDir, "beanstore_enumtest.kryo");
		if (file.exists()) {
			throw new AssertionError();
		}
		BeanStoreFactory factory = BeanStoreFactory.of(file);
		factory.registerEntity(LogEntry.class);
		
		var store = factory.create();
		var tx = store.transaction();
		var logEntry = tx.create(LogEntry.class);
		logEntry.setLogLevel(LogLevel.Error);
		tx.execute();
	}
	
}
