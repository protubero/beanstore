package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.factory.BeanStoreFactory;
import de.protubero.beanstore.model.Note;
import de.protubero.beanstore.model.Priority;
import de.protubero.beanstore.plugins.tags.Tag;
import de.protubero.beanstore.plugins.tags.TagExtension;
import de.protubero.beanstore.plugins.tags.TagGroup;
import de.protubero.beanstore.plugins.tags.TagManager;

public class TagTest {

	@TempDir
	File pFileDir;	

	@Test
	public void test() throws InterruptedException, ExecutionException {
		TagGroup group = TagManager.instance().newGroup("tagGroup");
		Tag tag = group.newTag("tag");
		Tag otherTag = group.newTag("othertag");
		
		File file = new File(pFileDir, "beanstore_" + getClass().getSimpleName() + ".kryo");
		BeanStoreFactory factory = BeanStoreFactory.init(file);
		TagExtension.init(factory);
		factory.registerEntity(Note.class);
		var store = factory.create();
		
		var tx = store.transaction();

		var note = tx.create(Note.class);
		note.setText("someText");
		
		var note2 = tx.create(Note.class);
		note2.setText("someOtherText");
		// note2.setPriority(Priority.High);
		note2.tagWith(tag);
		
		tx.execute().get();
		Long note2Id = note2.id();
		System.out.println("Note ID= " + note2Id);
		
		store.close();
		
		factory = BeanStoreFactory.init(file);
		TagExtension.init(factory);
		factory.registerEntity(Note.class);
		store = factory.create();
		
		Note secondNote = store.state().entity(Note.class).find(note2Id);
		assertEquals(1, secondNote.getTags().size());
		// assertEquals(Priority.High, secondNote.getPriority());
		assertTrue(secondNote.getTags().contains(tag));
		assertSame(tag, secondNote.getTags().findFirst(group).get());
		assertFalse(secondNote.getTags().contains(otherTag));
	}		
	
}

