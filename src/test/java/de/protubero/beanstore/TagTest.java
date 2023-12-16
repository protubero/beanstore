package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.api.BeanStoreFactory;
import de.protubero.beanstore.model.Note;
import de.protubero.beanstore.plugins.tags.Tag;
import de.protubero.beanstore.plugins.tags.TagGroup;
import de.protubero.beanstore.plugins.tags.TagManager;

public class TagTest {

	@TempDir
	File pFileDir;	

	@Test
	public void test() {
		TagGroup group = TagManager.instance().newGroup("tagGroup");
		Tag tag = group.newTag("tag");
		Tag otherTag = group.newTag("othertag");
		
		File file = new File(pFileDir, "beanstore_" + getClass().getSimpleName() + ".kryo");
		BeanStoreFactory factory = BeanStoreFactory.of(file);
		factory.registerEntity(Note.class);
		var store = factory.create();
		
		var tx = store.transaction();

		var note = tx.create(Note.class);
		note.setText("someText");
		
		var note2 = tx.create(Note.class);
		note2.setText("someOtherText");
		note2.getTags().plus(tag);
		
		tx.execute();
		Long note2Id = note2.id();
		
		store.close();

		factory = BeanStoreFactory.of(file);
		factory.registerEntity(Note.class);
		store = factory.create();
		
		Note secondNote = store.state().entity(Note.class).find(note2Id);
		assertTrue(secondNote.getTags().contains(tag));
		assertSame(tag, secondNote.getTags().findFirst(group).get());
		assertFalse(secondNote.getTags().contains(otherTag));
	}		
	
}

