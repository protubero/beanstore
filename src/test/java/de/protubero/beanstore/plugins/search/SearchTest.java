package de.protubero.beanstore.plugins.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.model.Employee;
import de.protubero.beanstore.persistence.impl.NoOpPersistence;

public class SearchTest {

	
	@Test
	public void test() {
		SearchEngine engine = new SearchEngine();
		engine.index(SearchEngineAction.create("employee", 1, "Erik the Wikinger"));
		engine.index(SearchEngineAction.create("employee", 2, "Robin the the Hood"));
		
		List<SearchResult> searchResult = engine.query("Erik");
		
		assertEquals(1, searchResult.size());
		assertEquals(1l, searchResult.get(0).id());
		assertEquals("employee", searchResult.get(0).getType());
		
		searchResult = engine.query("the");

		assertEquals(2, searchResult.size());
		assertEquals(2l, searchResult.get(0).id());
		assertEquals("employee", searchResult.get(0).getType());
		assertEquals(1l, searchResult.get(1).id());
		assertEquals("employee", searchResult.get(1).getType());
		
		engine.index(SearchEngineAction.update("employee", 1, "Erik Wikinger"));
		searchResult = engine.query("the");
		assertEquals(1, searchResult.size());
		assertEquals(2l, searchResult.get(0).id());
		assertEquals("employee", searchResult.get(0).getType());
		
		engine.index(SearchEngineAction.delete("employee", 1));
		searchResult = engine.query("Erik");
		assertEquals(0, searchResult.size());
		
		engine.close();
	}

	@Test
	public void test2() throws InterruptedException, ExecutionException {
		BeanStoreSearchPlugin searchPlugin = new BeanStoreSearchPlugin(); 

		BeanStoreBuilder builder = BeanStoreBuilder.init(NoOpPersistence.create());
		builder.addPlugin(searchPlugin);
		
		builder.registerEntity(Employee.class);
		searchPlugin.register(Employee.class, emp -> {
			return emp.getFirstName() + " " + emp.getLastName();
		});
		
		BeanStore beanStore = builder.build();
				
		var tx = beanStore.transaction();
		Employee emp = tx.create(Employee.class);
		emp.setFirstName("Wilhelm");
		emp.setLastName("the Conquerer");
		emp.setAge(33);
		tx.execute();
		
		searchPlugin.waitForCompletion();
		List<AbstractPersistentObject> searchResult = searchPlugin.search("Wilhelm");
		assertEquals(1, searchResult.size());
		assertEquals("the Conquerer", searchResult.get(0).get("lastName"));
		
	}
	
}
