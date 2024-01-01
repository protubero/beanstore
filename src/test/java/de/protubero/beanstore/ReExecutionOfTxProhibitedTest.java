package de.protubero.beanstore;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.factory.BeanStoreFactory;
import de.protubero.beanstore.model.Note;

public class ReExecutionOfTxProhibitedTest {
	
	@Test
	public void test() {
		BeanStoreFactory factory = BeanStoreFactory.init();
		factory.registerEntity(Note.class);
		
		BeanStore store = factory.create();
		var tx = store.transaction();
		var note = tx.create(Note.class);
		note.setText("AText");
		tx.executeBlocking();
		
		Assertions.assertThrows(Exception.class, () -> {tx.executeBlocking();});		
	}	
}
