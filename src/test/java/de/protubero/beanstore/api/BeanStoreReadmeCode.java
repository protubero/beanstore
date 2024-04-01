package de.protubero.beanstore.api;

import java.io.File;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.Entity;
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;

public class BeanStoreReadmeCode {

	@Entity(alias = "todo")
	public static class ToDo extends AbstractEntity {

		private String text;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
	
	@Test
	public void quickstart(@TempDir File tempDir) {


		// 2. Create and configure the Beanstore builder, register the data bean class
		KryoConfiguration kryoConfig = KryoConfiguration.create();
		KryoPersistence persistence = KryoPersistence.of(new File(tempDir, "file.bst"), kryoConfig);
		BeanStoreBuilder builder = BeanStoreBuilder.init(persistence);
		builder.registerEntity(ToDo.class);

		// 3. Create the BeanStore
		BeanStore store = builder.build();

		// 4. Create a new instance using a transaction
		var tx = store.transaction();
		ToDo newToDo = tx.create(ToDo.class);
		newToDo.setText("Hello World");
		tx.execute();

		// 5. read a list of all ToDos
		var allToDos = store.snapshot().entity(ToDo.class).stream().collect(Collectors.toList());
		
		allToDos.forEach(System.out::println);
	}
	
	@Test
	public void kryoConfiguration() {
		KryoConfiguration kryoConfig = KryoConfiguration.create();
	}
	
}
