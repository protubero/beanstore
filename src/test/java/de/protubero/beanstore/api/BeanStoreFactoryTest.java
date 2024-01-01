package de.protubero.beanstore.api;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.factory.BeanStoreFactory;
import de.protubero.beanstore.model.Employee;
import de.protubero.beanstore.model.Note;
import de.protubero.beanstore.model.PostCode;
import de.protubero.beanstore.plugins.txlog.BeanStoreTransactionLogPlugin;

public class BeanStoreFactoryTest {

	
	@Test
	public void exceptionOnUnregisteredEntity(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreFactory builder = BeanStoreFactory.init(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		builder.registerEntity(Employee.class);
		var store = builder.create();
		
		var tx = store.transaction();
		var employee1 = tx.create(Employee.class);
		employee1.setFirstName("John");
		employee1.setLastName("Lennon");		
		employee1.setAge(44);		
		
		tx.execute();
		store.close();
		
		var builder2 = BeanStoreFactory.init(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		Assertions.assertThrows(Exception.class, () -> {builder2.create();});
				
	}	
	
	@Test
	public void mixingRegisteredAndUnregisteredEntities(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreFactory builder = BeanStoreFactory.init(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		builder.registerEntity(Employee.class);
		var store = builder.create();
		
		var tx = store.transaction();
		var employee1 = tx.create(Employee.class);
		employee1.setFirstName("John");
		employee1.setLastName("Lennon");		
		employee1.setAge(44);		
		
		tx.execute();
		store.close();
		
		var builder2 = BeanStoreFactory.init(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		builder2.registerEntity(Employee.class);
		builder2.registerEntity(Note.class);
		store = builder2.create();
		
		Assertions.assertEquals(1, store.state().entity(Employee.class).count());
		Assertions.assertEquals(0, store.state().entity(Note.class).count());				
	}	
	
	
	@Test
	public void errorWhenChangingFactoryAfterStoreCreation(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreFactory builder = BeanStoreFactory.init(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		builder.registerEntity(Employee.class);
		var store = builder.create();
		
		Assertions.assertThrows(Exception.class, () -> { builder.addMigration("xyz", tx -> {});});
		Assertions.assertThrows(Exception.class, () -> {builder.initNewStore(tx -> {});});
		Assertions.assertThrows(Exception.class, () -> {builder.registerKryoPropertyBean(PostCode.class);});
		// builder.kryoConfig().register(PostCode.class, new PropertyBean);
		Assertions.assertThrows(Exception.class, () -> {builder.addPlugin(new BeanStoreTransactionLogPlugin());});
		Assertions.assertThrows(Exception.class, () -> {builder.create();});
	}
	
	@Test
	public void errorIfNoEntityIsRegistered(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreFactory builder = BeanStoreFactory.init(new File(tempDir, getClass().getSimpleName() + ".kryo"));
		Assertions.assertThrows(Exception.class, () -> {builder.create();});
	}	

}
