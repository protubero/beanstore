package de.protubero.beanstore.api;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.model.Employee;
import de.protubero.beanstore.model.Note;
import de.protubero.beanstore.model.PostCode;
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;
import de.protubero.beanstore.persistence.kryo.PropertyBeanSerializer;
import de.protubero.beanstore.plugins.txlog.BeanStoreTransactionLogPlugin;

public class BeanStoreBuilderTest {

	
	@Test
	public void exceptionOnUnregisteredEntity(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder.registerEntity(Employee.class);
		var store = builder.build();
		
		var tx = store.transaction();
		var employee1 = tx.create(Employee.class);
		employee1.setFirstName("John");
		employee1.setLastName("Lennon");		
		employee1.setAge(44);		
		
		tx.executeAsync();
		store.close();
		
		var builder2 = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		Assertions.assertThrows(Exception.class, () -> {builder2.build();});
				
	}	
	
	@Test
	public void mixingRegisteredAndUnregisteredEntities(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder.registerEntity(Employee.class);
		var store = builder.build();
		
		var tx = store.transaction();
		var employee1 = tx.create(Employee.class);
		employee1.setFirstName("John");
		employee1.setLastName("Lennon");		
		employee1.setAge(44);		
		
		tx.execute();
		store.close();
		
		var builder2 = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		builder2.registerEntity(Employee.class);
		builder2.registerEntity(Note.class);
		store = builder2.build();
		
		Assertions.assertEquals(1, store.snapshot().entity(Employee.class).count());
		Assertions.assertEquals(0, store.snapshot().entity(Note.class).count());				
	}	
	
	
	@Test
	public void errorWhenChangingFactoryAfterStoreCreation(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		KryoConfiguration kryoConfig = KryoConfiguration.create();
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), kryoConfig));
		builder.registerEntity(Employee.class);
		@SuppressWarnings("unused")
		var store = builder.build();
		
		Assertions.assertThrows(Exception.class, () -> { builder.addMigration("xyz", tx -> {});});
		Assertions.assertThrows(Exception.class, () -> {builder.initNewStore(tx -> {});});
		Assertions.assertThrows(Exception.class, () -> {kryoConfig.register(PostCode.class, PropertyBeanSerializer.class, 300);});
		// builder.kryoConfig().register(PostCode.class, new PropertyBean);
		Assertions.assertThrows(Exception.class, () -> {builder.addPlugin(new BeanStoreTransactionLogPlugin());});
		Assertions.assertThrows(Exception.class, () -> {builder.build();});
	}
	
	@Test
	public void errorIfNoEntityIsRegistered(@TempDir File tempDir) throws InterruptedException, ExecutionException  {
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(tempDir, getClass().getSimpleName() + ".kryo"), KryoConfiguration.create()));
		Assertions.assertThrows(Exception.class, () -> {builder.build();});
	}	

}
