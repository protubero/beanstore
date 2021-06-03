package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreFactory;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.plugins.search.BeanStoreSearchPlugin;
import de.protubero.beanstore.plugins.search.SearchEngine;
import de.protubero.beanstore.plugins.search.SearchEngineAction;
import de.protubero.beanstore.plugins.search.SearchResult;

public class SearchTest {

	
	@Test
	public void test() {
		SearchEngine engine = new SearchEngine();
		engine.index(SearchEngineAction.create("employee", "1", "Erik the Wikinger"));
		engine.index(SearchEngineAction.create("employee", "2", "Robin the the Hood"));
		
		List<SearchResult> searchResult = engine.query("Erik");
		
		assertEquals(1, searchResult.size());
		assertEquals("1", searchResult.get(0).getId());
		assertEquals("employee", searchResult.get(0).getType());
		
		searchResult = engine.query("the");

		assertEquals(2, searchResult.size());
		assertEquals("2", searchResult.get(0).getId());
		assertEquals("employee", searchResult.get(0).getType());
		assertEquals("1", searchResult.get(1).getId());
		assertEquals("employee", searchResult.get(1).getType());
		
		engine.index(SearchEngineAction.update("employee", "1", "Erik Wikinger"));
		searchResult = engine.query("the");
		assertEquals(1, searchResult.size());
		assertEquals("2", searchResult.get(0).getId());
		assertEquals("employee", searchResult.get(0).getType());
		
		engine.index(SearchEngineAction.delete("employee", "1"));
		searchResult = engine.query("Erik");
		assertEquals(0, searchResult.size());
		
		engine.close();
	}

	@Test
	public void test2() throws InterruptedException, ExecutionException {
		BeanStoreSearchPlugin searchPlugin = new BeanStoreSearchPlugin(); 

		BeanStoreFactory factory = BeanStoreFactory.createNonPersisted();
		factory.addPlugin(searchPlugin);
		
		var entity = factory.registerType(Employee.class);
		searchPlugin.register(entity, emp -> {
			return emp.getFirstName() + " " + emp.getLastName();
		});
		
		BeanStore beanStore = factory.create();
				
		var tx = beanStore.transaction();
		Employee emp = tx.create(Employee.class);
		emp.setFirstName("Wilhelm");
		emp.setLastName("the Conquerer");
		emp.setAge(33);
		tx.execute();
		
		Thread.sleep(1000l);
		
		List<AbstractPersistentObject> searchResult = searchPlugin.search("Wilhelm");
		assertEquals(1, searchResult.size());
		assertEquals("the Conquerer", searchResult.get(0).get("lastName"));
		
	}
	
}
