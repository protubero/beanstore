package de.protubero.beanstore.plugins.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanStoreEntity;
import de.protubero.beanstore.base.StoreSnapshot;
import de.protubero.beanstore.init.BeanStore;
import de.protubero.beanstore.init.BeanStorePlugin;

public class BeanStoreSearchPlugin implements BeanStorePlugin {

	public static final Logger log = LoggerFactory.getLogger(BeanStoreSearchPlugin.class);
	
	private Map<BeanStoreEntity<?>, Function<? extends AbstractPersistentObject, String>> titleContentProjectionMap = new HashMap<>();
	private SearchEngine searchEngine;
	private BeanStore beanStore;

	public  <X extends AbstractPersistentObject> void register(BeanStoreEntity<X> entity, Function<X, String> titleContentProjection) {
		if (titleContentProjectionMap.put(entity, Objects.requireNonNull(titleContentProjection)) != null) {
			throw new RuntimeException("duplicate registration of entity "+ entity);
		}
	}	
	
	public List<AbstractPersistentObject> search(String queryString) {
		List<AbstractPersistentObject> searchResult = beanStore.resolve(searchEngine.query(queryString));
		return searchResult;
	}

	@Override
	public void onEndCreate(BeanStore beanStore, StoreSnapshot snapshot) {
		this.beanStore = beanStore;
		
		// init search
		searchEngine = new SearchEngine();
		SearchEngineAdapter searchAdapter = new SearchEngineAdapter(searchEngine, titleContentProjectionMap);
		
		beanStore.onChangeInstanceAsync(searchAdapter);
						
		// index asynchcronically
		new Thread(() -> {
			log.info("Start initial indexing");
			AtomicInteger counter = new AtomicInteger();
			snapshot.getSnapshotMap().forEach((alias, coll) -> {
				coll.forEach(apo -> {
					counter.getAndIncrement();
					searchAdapter.accept(apo);
				});
			});
			log.info("Stop initial indexing (" + counter.get() + ")");
			
			// start processing transactions
			searchAdapter.start();
		}).start();
	}


}
