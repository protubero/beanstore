package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.api.PersistentProperty;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.kryo.KryoConfigurationImpl;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;

class PersistenceTest {


	@Test
	void test(@TempDir File tempDir) {
		File tempFile = new File(tempDir, "kryoFile.kry");
		
		KryoPersistence persistence = new KryoPersistence(new KryoConfigurationImpl(), tempFile);

		Instant now = Instant.now();
				
		PersistentTransaction pt = new PersistentTransaction();
		pt.create("ealias", 55, PersistentProperty.of("eins", now));
		persistence.writer().append(pt);
		
		pt = new PersistentTransaction();
		pt.update("ealias", 55, PersistentProperty.of("eins", 5));
		persistence.writer().append(pt);

		pt = new PersistentTransaction();
		pt.delete("ealias", 55);
		persistence.writer().append(pt);
		
		assertTrue(tempFile.exists());
		
		List<PersistentTransaction> loadedTransactions = persistence.reader().load();
		assertEquals(3, loadedTransactions.size());
		
		PersistentTransaction lt = loadedTransactions.get(0);
		assertEquals(1, lt.getInstanceTransactions().length);
		assertEquals(1, lt.getInstanceTransactions()[0].getPropertyUpdates().length);
		assertEquals(PersistentInstanceTransaction.TYPE_CREATE, lt.getInstanceTransactions()[0].getType());
		assertEquals(55, lt.getInstanceTransactions()[0].getId());
		assertEquals("ealias", lt.getInstanceTransactions()[0].getAlias());
		assertEquals("eins", lt.getInstanceTransactions()[0].getPropertyUpdates()[0].getProperty());
		assertEquals(now, lt.getInstanceTransactions()[0].getPropertyUpdates()[0].getValue());
		
		lt = loadedTransactions.get(1);
		assertEquals(1, lt.getInstanceTransactions().length);
		assertEquals(PersistentInstanceTransaction.TYPE_UPDATE, lt.getInstanceTransactions()[0].getType());
		assertEquals(1, lt.getInstanceTransactions()[0].getPropertyUpdates().length);
		assertEquals(55, lt.getInstanceTransactions()[0].getId());
		assertEquals("ealias", lt.getInstanceTransactions()[0].getAlias());
		assertEquals("eins", lt.getInstanceTransactions()[0].getPropertyUpdates()[0].getProperty());
		assertEquals(5, lt.getInstanceTransactions()[0].getPropertyUpdates()[0].getValue());

		lt = loadedTransactions.get(2);
		assertEquals(1, lt.getInstanceTransactions().length);
		assertEquals(PersistentInstanceTransaction.TYPE_DELETE, lt.getInstanceTransactions()[0].getType());
		assertNull(lt.getInstanceTransactions()[0].getPropertyUpdates());
		assertEquals(55, lt.getInstanceTransactions()[0].getId());
		assertEquals("ealias", lt.getInstanceTransactions()[0].getAlias());
	}

	@Test
	void test2(@TempDir File tempDir) {
		File tempFile = new File(tempDir, "kryoFile2.kry");
		
		KryoPersistence persistence = new KryoPersistence(new KryoConfigurationImpl(), tempFile);

		Instant now = Instant.now();
				
		PersistentTransaction pt = new PersistentTransaction();
		pt.create("ealias", 55, PersistentProperty.of("eins", now));
		pt.update("ealias", 55, PersistentProperty.of("eins", 5));
		pt.delete("ealias", 55);
		persistence.writer().append(pt);
		
		assertTrue(tempFile.exists());
		
		List<PersistentTransaction> loadedTransactions = persistence.reader().load();
		assertEquals(1, loadedTransactions.size());
		
		PersistentTransaction lt = loadedTransactions.get(0);
		assertEquals(3, lt.getInstanceTransactions().length);
		
		assertEquals(1, lt.getInstanceTransactions()[0].getPropertyUpdates().length);
		assertEquals(PersistentInstanceTransaction.TYPE_CREATE, lt.getInstanceTransactions()[0].getType());
		assertEquals(55, lt.getInstanceTransactions()[0].getId());
		assertEquals("ealias", lt.getInstanceTransactions()[0].getAlias());
		assertEquals("eins", lt.getInstanceTransactions()[0].getPropertyUpdates()[0].getProperty());
		assertEquals(now, lt.getInstanceTransactions()[0].getPropertyUpdates()[0].getValue());
		
		assertEquals(PersistentInstanceTransaction.TYPE_UPDATE, lt.getInstanceTransactions()[1].getType());
		assertEquals(1, lt.getInstanceTransactions()[1].getPropertyUpdates().length);
		assertEquals(55, lt.getInstanceTransactions()[1].getId());
		assertEquals("ealias", lt.getInstanceTransactions()[1].getAlias());
		assertEquals("eins", lt.getInstanceTransactions()[1].getPropertyUpdates()[0].getProperty());
		assertEquals(5, lt.getInstanceTransactions()[1].getPropertyUpdates()[0].getValue());

		assertEquals(PersistentInstanceTransaction.TYPE_DELETE, lt.getInstanceTransactions()[2].getType());
		assertNull(lt.getInstanceTransactions()[2].getPropertyUpdates());
		assertEquals(55, lt.getInstanceTransactions()[2].getId());
		assertEquals("ealias", lt.getInstanceTransactions()[2].getAlias());
	}
}
