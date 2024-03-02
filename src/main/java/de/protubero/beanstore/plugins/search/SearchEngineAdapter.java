package de.protubero.beanstore.plugins.search;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.BeanStoreEntity;
import de.protubero.beanstore.tx.InstanceTransactionEvent;

class SearchEngineAdapter implements Consumer<InstanceTransactionEvent<?>> {

	public static final Logger log = LoggerFactory.getLogger(SearchEngineAdapter.class);
	
	
	private BlockingQueue<SearchEngineAction> actionQueue = new LinkedBlockingQueue<>();
	private Thread thread;
	private SearchEngine searchEngine;
	
	private boolean indexInTransaction = false;
	
	
	private Map<Object, Function<? extends AbstractPersistentObject, String>> titleContentProjectionMap;
	
	public SearchEngineAdapter(SearchEngine searchEngine, Map<Object, Function<? extends AbstractPersistentObject, String>> titleContentProjectionMap, boolean aIndexInTransaction) {
		this.titleContentProjectionMap = titleContentProjectionMap;
		this.searchEngine = searchEngine;
		this.indexInTransaction = aIndexInTransaction;
		
		if (!indexInTransaction) {
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
	}

	public void start() {
		if (!indexInTransaction) {
			thread.start();
		}	
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
		if (indexInTransaction) {
			searchEngine.index(action);
		} else {
			try {
				actionQueue.put(action);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}	
	}

	@Override
	public void accept(InstanceTransactionEvent<?> it) {
		index(it);
	}

	private void index(InstanceTransactionEvent<?> it) throws AssertionError {
		String content = null;
		SearchEngineAction action = null;
		switch (it.type()) {
		case Create:
			content = contentOf(it.newInstance());
			if (content == null) {
				return;
			}
			action = SearchEngineAction.create(it.entity().alias(), it.newInstance().id(), content);
			break;
		case Update:
			content = contentOf(it.newInstance());
			if (content == null) {
				return;
			}
			action = SearchEngineAction.update(it.entity().alias(), it.newInstance().id(), content);
			break;
		case Delete:
			action = SearchEngineAction.delete(it.entity().alias(), it.replacedInstance().id());
			break;
		default:
			throw new AssertionError();
		}
		enqueue(action);
	}

	@SuppressWarnings("unchecked")
	protected String contentOf(AbstractPersistentObject instance) {
		BeanStoreEntity<?> entity = instance.entity();
		Function<? extends AbstractPersistentObject, String> titleContentProjection = titleContentProjectionMap.get(entity.entityClass());
		if (titleContentProjection == null) {
			titleContentProjection = titleContentProjectionMap.get(entity.alias());
		}
		if (titleContentProjection != null) {
			return ((Function<AbstractPersistentObject, String>) titleContentProjection).apply(instance);
		}
		return null;
	}
	
}
