package de.protubero.beanstore.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreFactory;
import de.protubero.beanstore.api.EntityState;
import de.protubero.beanstore.base.entity.MapObject;
import de.protubero.beanstore.model.Note;

public class InitTransactionTest {
	
	
	@TempDir
	File pFileDir;	
	
	@Test
	public void test() {
		BeanStoreFactory factory = createFactory();
		
		factory.initNewStore(tx -> {
			var todo = tx.create("todo");
			todo.put("text", "Write more tests");
			
			Note note = tx.create(Note.class);
			note.setText("My Text");
		});
		
		checkStoreData(factory);
		
		// has it been persisted accordingly?
		BeanStoreFactory newFactory = createFactory();
		
		checkStoreData(newFactory);
	}
	
	@Test 
	public void onlyOneInitTransactionAllowed() {
		BeanStoreFactory factory = createFactory();
		
		factory.initNewStore(tx -> {});
		Assertions.assertThrows(Exception.class, () -> {factory.initNewStore(tx -> {});});
	}

	private BeanStoreFactory createFactory() {
		BeanStoreFactory factory = BeanStoreFactory.of(new File(pFileDir, getClass().getSimpleName() + ".kryo"));
		factory.registerEntity(Note.class);
		factory.registerMapEntity("todo");
		return factory;
	}

	private void checkStoreData(BeanStoreFactory factory) {
		// check that init transaction has been executed
		BeanStore store = factory.create();
		EntityState<Note> noteStore = store.state().entity(Note.class);
		assertEquals(1, noteStore.count());
		Note note = noteStore.asList().get(0);
		assertEquals("My Text", note.getText());
		
		EntityState<MapObject> todoStore = store.state().entity("todo");
		assertEquals(1, todoStore.count());
		MapObject todo = todoStore.asList().get(0);
		assertEquals("Write more tests", todo.get("text"));
	}	
}
