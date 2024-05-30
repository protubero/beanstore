package de.protubero.beanstore.linksandlabels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.links.Link;
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;

public class LinkTest {

	@Test
	public void linkTest(@TempDir File tempDir) {
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder.registerMapEntity("note");
		builder.enableLinks();
		BeanStore store = builder.build();
		
		ExecutableBeanStoreTransaction tx = store.transaction();
		var newNote1 = tx.create("note");
		var newNote2 = tx.create("note");
		
		tx.execute();
		
		tx = store.transaction();
		tx.link(newNote1, newNote2, "test");
		tx.execute();
		
		newNote1 = store.get(newNote1);
		newNote2 = store.get(newNote2);
		
		Set<Link<?, ?>> linkSet1 = newNote1.links().asSet();
		Set<Link<?, ?>> linkSet2 = newNote2.links().asSet();
		
		assertEquals(1, linkSet1.size());
		assertEquals(1, linkSet2.size());
		
		Link<?, ?> firstLink = linkSet1.iterator().next();
		Link<?, ?> secondLink = linkSet2.iterator().next();
		
		assertSame(firstLink, secondLink);
		
		assertSame(firstLink.source(), newNote1);
		assertSame(firstLink.target(), newNote2);
		
		store.close();
		
		builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder.registerMapEntity("note");
		builder.enableLinks();
		store = builder.build();
		
		newNote1 = store.get(newNote1);
		newNote2 = store.get(newNote2);
		
		linkSet1 = newNote1.links().asSet();
		linkSet2 = newNote2.links().asSet();
		
		assertEquals(1, linkSet1.size());
		assertEquals(1, linkSet2.size());
		
		firstLink = linkSet1.iterator().next();
		secondLink = linkSet2.iterator().next();
		
		assertSame(firstLink, secondLink);
		
		assertSame(firstLink.source(), newNote1);
		assertSame(firstLink.target(), newNote2);

		tx = store.transaction();
		firstLink.delete(tx);
		tx.execute();
		
				
	}
	
}
