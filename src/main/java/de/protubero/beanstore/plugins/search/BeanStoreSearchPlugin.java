package de.protubero.beanstore.plugins.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreSnapshot;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;

public class BeanStoreSearchPlugin implements BeanStorePlugin {

	public static final Logger log = LoggerFactory.getLogger(BeanStoreSearchPlugin.class);
	
	private Map<Object, Function<? extends AbstractPersistentObject, String>> titleContentProjectionMap = new HashMap<>();
	private SearchEngine searchEngine;
	private BeanStore beanStore;
	
	private SearchEngineAdapter searchAdapter;
	
	public void register(String entityAlias, Function<AbstractPersistentObject, String> titleContentProjection) {
		if (titleContentProjectionMap.put(entityAlias, Objects.requireNonNull(titleContentProjection)) != null) {
			throw new RuntimeException("duplicate registration of entity "+ entityAlias);
		}
	}

	public <X extends AbstractPersistentObject> void register(Class<X> entityClass, Function<X, String> titleContentProjection) {
		if (titleContentProjectionMap.put(entityClass, Objects.requireNonNull(titleContentProjection)) != null) {
			throw new RuntimeException("duplicate registration of entity "+ entityClass);
		}
	}	
	
	public List<AbstractPersistentObject> search(String queryString) {
		List<AbstractPersistentObject> searchResult = beanStore.snapshot().resolveAll(searchEngine.query(queryString));
		return searchResult;
	}

	@Override
	public void onEndCreate(BeanStore beanStore) {
		this.beanStore = beanStore;
		
		// init search
		searchEngine = new SearchEngine();
		searchAdapter = new SearchEngineAdapter(searchEngine, titleContentProjectionMap);
		
		beanStore.callbacks().onChangeInstanceAsync(searchAdapter);

		BeanStoreSnapshot snapshot = beanStore.snapshot();
		
		// index asynchcronically
		new Thread(() -> {
			log.info("Start initial indexing");
			AtomicInteger counter = new AtomicInteger();
			
			for (var era : snapshot) {
				for (var apo : era) {
					counter.getAndIncrement();
					searchAdapter.accept(apo);
				}
			}	
			log.info("Stop initial indexing (" + counter.get() + ")");
			
			// start processing transactions
			searchAdapter.start();
		}).start();
	}

	public void waitForCompletion() {
		searchAdapter.waitForCompletion();
	}

}
