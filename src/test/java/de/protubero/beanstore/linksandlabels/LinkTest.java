package de.protubero.beanstore.linksandlabels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.entity.Keys;
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;

public class LinkTest {

	@Test
	public void linkTest(@TempDir File tempDir) {
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder.registerMapEntity("note");
		BeanStore store = builder.build();

		ExecutableBeanStoreTransaction tx = store.transaction();
		var newNote1 = tx.create("note");
		var newNote2 = tx.create("note");
		
		tx.execute();
		
		tx = store.transaction();
		tx.update(newNote1).addLinks(LinkValue.of(newNote2));
		tx.execute();
		
		newNote1 = store.get(newNote1);
		newNote2 = store.get(newNote2);

		var snapshot =  store.snapshot();
		assertEquals(1, snapshot.links().stream().count());
		Link<?, ?> link = snapshot.links().stream().findFirst().get();
		assertNull(link.type());
		assertSame(snapshot.get(newNote1), link.source());
		assertSame(snapshot.get(newNote2), link.target());
		
		tx = store.transaction();
		tx.update(newNote2).addLinks(LinkValue.of(newNote1, "succ"));
		tx.execute();
		
		snapshot = store.snapshot();
		snapshot.links().stream().forEach(l -> {
			System.out.println(l);
		});
		
		assertEquals(2, snapshot.links().stream().count());
		link = snapshot.links().stream().filter(l -> l.type() != null).findFirst().get();
		assertEquals("succ", link.type());
		assertSame(snapshot.get(newNote2), link.source());
		assertSame(snapshot.get(newNote1), link.target());

		store.close();
		
		builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder.registerMapEntity("note");
		store = builder.build();
		
		snapshot = store.snapshot();
		assertEquals(2, snapshot.links().stream().count());
		link = snapshot.links().stream().filter(l -> l.type() != null).findFirst().get();
		assertEquals("succ", link.type());
		assertSame(snapshot.get(newNote2), link.source());
		assertSame(snapshot.get(newNote1), link.target());
				
		tx = store.transaction();
		tx.delete(Keys.key(newNote1));
		tx.execute();
		
	}
	
}
