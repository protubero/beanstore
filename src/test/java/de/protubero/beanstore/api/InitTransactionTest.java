package de.protubero.beanstore.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.entity.MapObject;
import de.protubero.beanstore.model.Note;

public class InitTransactionTest {
	
	
	@TempDir
	File pFileDir;	
	
	@Test
	public void test() {
		BeanStoreBuilder builder = createBuilder();
		
		builder.initNewStore(tx -> {
			var todo = tx.create("todo");
			todo.put("text", "Write more tests");
			
			Note note = tx.create(Note.class);
			note.setText("My Text");
		});
		
		checkStoreData(builder);
		
		// has it been persisted accordingly?
		BeanStoreBuilder newBuilder = createBuilder();
		
		checkStoreData(newBuilder);
	}
	
	@Test 
	public void onlyOneInitTransactionAllowed() {
		BeanStoreBuilder builder = createBuilder();
		
		builder.initNewStore(tx -> {});
		Assertions.assertThrows(Exception.class, () -> {builder.initNewStore(tx -> {});});
	}

	private BeanStoreBuilder createBuilder() {
		BeanStoreBuilder builder = BeanStoreBuilder.init(new File(pFileDir, getClass().getSimpleName() + ".kryo"));
		builder.registerEntity(Note.class);
		builder.registerMapEntity("todo");
		return builder;
	}

	private void checkStoreData(BeanStoreBuilder builder) {
		// check that init transaction has been executed
		BeanStore store = builder.build();
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
