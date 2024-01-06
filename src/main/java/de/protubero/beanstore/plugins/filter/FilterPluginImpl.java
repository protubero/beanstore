package de.protubero.beanstore.plugins.filter;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;
import de.protubero.beanstore.tx.InstanceTransactionEvent;
import de.protubero.beanstore.tx.TransactionEvent;
import de.protubero.beanstore.tx.TransactionPhase;

public class FilterPluginImpl<T extends AbstractPersistentObject> implements FilterPlugin, BeanStorePlugin {

	private Predicate<T> predicate;
	private String alias;
	private Class<T> entityClass;
	private boolean asyncUpdate = false;
	private HashPMap<Long, T> objectMap = HashTreePMap.empty(); 

	public FilterPluginImpl(String alias, Predicate<T> predicate) {
		this.predicate = predicate;
	}
	
	public FilterPluginImpl(Class<T> entityClass, Predicate<T> predicate) {
		this.predicate = predicate;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onEndCreate(BeanStore beanStore) {
		Consumer<TransactionEvent> consumer = new Consumer<>() {

			@Override
			public void accept(TransactionEvent event) {
				HashPMap<Long, T> newObjectMap = FilterPluginImpl.this.objectMap;
			
				if ((event.phase() != TransactionPhase.COMMITTED_SYNC) &&
						(event.phase() != TransactionPhase.COMMITTED_ASYNC)) {
					throw new AssertionError();
				}
				for (InstanceTransactionEvent<?> ite : event.getInstanceEvents()) {
					if ((entityClass != null && ite.entity().entityClass() == entityClass) 
							|| (ite.entity().alias().equals(alias))) {
						switch (ite.type()) {
						case Create:
						case Update:
							newObjectMap = newObjectMap.plus(ite.instanceId(), (T) ite.newInstance());
							break;
						case Delete:
							newObjectMap = newObjectMap.minus(ite.instanceId());
							break;
						}
					}
				}
				if (newObjectMap != FilterPluginImpl.this.objectMap) {
					FilterPluginImpl.this.objectMap = newObjectMap;
				}
			}
			
		};
		
		if (asyncUpdate) {
			beanStore.callbacks().onChangeAsync((Consumer) consumer);
		} else {
			beanStore.callbacks().onChange((Consumer) consumer);
		}
	}


	public String getAlias() {
		return alias;
	}

	public boolean isAsyncUpdate() {
		return asyncUpdate;
	}

	public void setAsyncUpdate(boolean asyncUpdate) {
		this.asyncUpdate = asyncUpdate;
	}
	
	public T get(Long id) {
		return objectMap.get(id);
	}
	
	public Optional<T> getOptional(Long id) {
		return Optional.ofNullable(objectMap.get(id));
	}

	public Stream<T> objects() {
		return objectMap.values().stream();
	}
	
	public int size() {
		return objectMap.size();
	}

	public boolean isEmpty() {
		return objectMap.isEmpty();
	}

	
}
