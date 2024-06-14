package de.protubero.beanstore.linksandlabels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;

public class LabelTest {

	@Test
	public void labelBasicTest(@TempDir File tempDir) {
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder.registerMapEntity("note");
		BeanStore store = builder.build();

		ExecutableBeanStoreTransaction tx = store.transaction();
		var newNote1 = tx.create("note");
		var newNote2 = tx.create("note");
		newNote2.addLabels("hot");
		tx.execute();
		
		tx = store.transaction();
		var upd1 = tx.update(newNote1);
		upd1.addLabels("cold");
		tx.execute();
		
		var n1 = store.get(newNote1);
		assertEquals(1, n1.getLabels().size());
		
		store.close();
		
		builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder.registerMapEntity("note");
		store = builder.build();
		
		n1 = store.get(n1);
		assertNotNull(n1);
		assertEquals(1, n1.getLabels().size());
		String lbl = n1.getLabels().iterator().next();
		assertEquals("cold", lbl);
		
		var n2 = store.get(newNote2);
		String lbl2 = n2.getLabels().iterator().next();
		assertEquals("hot", lbl2);
		
		tx = store.transaction();
		var upd2 = tx.update(n2);
		upd2.removeLabels("hot");
		tx.execute();
		store.close();
		
		builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder.registerMapEntity("note");
		store = builder.build();
		
		n2 = store.get(newNote2);
		assertEquals(0, n2.getLabels().size());
	}
	
	
}
