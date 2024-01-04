package de.protubero.beanstore.tx;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.model.Note;

public class ReExecutionOfTxProhibitedTest {
	
	@Test
	public void test() {
		BeanStoreBuilder builder = BeanStoreBuilder.init();
		builder.registerEntity(Note.class);
		
		BeanStore store = builder.build();
		var tx = store.transaction();
		var note = tx.create(Note.class);
		note.setText("AText");
		tx.executeBlocking();
		
		Assertions.assertThrows(Exception.class, () -> {tx.executeBlocking();});		
	}	
}
