package de.protubero.beanstore.plugins.search;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.base.BeanChange;
import de.protubero.beanstore.base.BeanStoreEntity;

class SearchEngineAdapter implements Consumer<BeanChange<?>> {

	public static final Logger log = LoggerFactory.getLogger(SearchEngineAdapter.class);
	
	
	private BlockingQueue<SearchEngineAction> actionQueue = new LinkedBlockingQueue<>();
	private Thread thread;
	private SearchEngine searchEngine;
	
	private Map<BeanStoreEntity<?>, Function<? extends AbstractPersistentObject, String>> titleContentProjectionMap;
	
	public SearchEngineAdapter(SearchEngine searchEngine, Map<BeanStoreEntity<?>, Function<? extends AbstractPersistentObject, String>> titleContentProjectionMap) {
		this.titleContentProjectionMap = titleContentProjectionMap;
		this.searchEngine = searchEngine;
		
		thread = new Thread(() -> {
			while (true) {
				try {
					SearchEngineAction action = actionQueue.take();
					searchEngine.index(action);
				} catch (InterruptedException e) {
					log.error("Search interrupted", e);
				}				
			}
		});
	}

	public void start() {
		thread.start();
	}
	
	void accept(AbstractPersistentObject apo) {
		String content = contentOf(apo);
		if (content == null) {
			return;
		}
		var action = SearchEngineAction.create(apo.entity().alias(), apo.id(), content);
		searchEngine.index(action);
	}
	
	private void enqueue(SearchEngineAction action) {
		try {
			actionQueue.put(action);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void accept(BeanChange<?> it) {
		index(it);
	}

	private void index(BeanChange<?> it) throws AssertionError {
		String content = null;
		SearchEngineAction action = null;
		switch (it.type()) {
		case Create:
			content = contentOf(it.newInstance());
			System.out.println(content);
			if (content == null) {
				return;
			}
			action = SearchEngineAction.create(it.entity().alias(), it.instanceId(), content);
			break;
		case Update:
			content = contentOf(it.newInstance());
			if (content == null) {
				return;
			}
			action = SearchEngineAction.update(it.entity().alias(), it.instanceId(), content);
			break;
		case Delete:
			action = SearchEngineAction.delete(it.entity().alias(), it.instanceId());
			break;
		default:
			throw new AssertionError();
		}
		enqueue(action);
	}

	@SuppressWarnings("unchecked")
	protected String contentOf(AbstractPersistentObject instance) {
		Function<? extends AbstractPersistentObject, String> titleContentProjection = titleContentProjectionMap.get(instance.entity());
		if (titleContentProjection != null) {
			return ((Function<AbstractPersistentObject, String>) titleContentProjection).apply(instance);
		}
		return null;
	}
	
}
