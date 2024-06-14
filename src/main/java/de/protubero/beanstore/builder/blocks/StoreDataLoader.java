package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.pcollections.PSet;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.entity.CompanionRegistry;
import de.protubero.beanstore.linksandlabels.UpdatePSet;
import de.protubero.beanstore.linksandlabels.ValueUpdateFunction;
import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.api.PersistentProperty;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.PersistentTransactionConsumer;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;

public final class StoreDataLoader {

	private int lastReadTransactionId;
	private TransactionPersistence persistence;
	private Consumer<PersistentTransaction> transactionListener;

	private StoreDataLoader(TransactionPersistence persistence, Consumer<PersistentTransaction> transactionListener) {
		this.persistence = Objects.requireNonNull(persistence);
		this.transactionListener = transactionListener;
	}

	private StoreDataLoader(TransactionPersistence persistence) {
		this(persistence, null);
	}

	public static StoreDataLoader of(TransactionPersistence persistence, Consumer<PersistentTransaction> transactionListener) {
		return new StoreDataLoader(persistence, transactionListener);
	}

	public static StoreDataLoader of(TransactionPersistence persistence) {
		return new StoreDataLoader(persistence);
	}
	
	public LoadedStoreData load(Integer state) {
		if (persistence.isEmpty()) {
			if (state != null) {
				throw new RuntimeException("Empty store has no state " + state);
			}
			return new LoadedStoreData(persistence, null, Collections.emptyList());
		} else {
			return load(persistence.reader(), state); 
		}
	}	

	
	public List<BeanStoreState> loadStates() {
		List<BeanStoreState> states = new ArrayList<>();
		persistence.reader().load(pt -> {
			states.add(transactionToState(pt));
		});
		
		
		return Collections.unmodifiableList(states);
	}

	private BeanStoreState transactionToState(PersistentTransaction pt) {
		BeanStoreState storeState = new BeanStoreState(
				pt.getMigrationId(), 
				pt.getTimestamp(), 
				pt.getTransactionType(),
				pt.getSeqNum(),
				pt.getDescription()
				);
		return storeState;
	}
	
	
	@SuppressWarnings("unchecked")
	private LoadedStoreData load(TransactionReader reader, Integer state) {
		lastReadTransactionId = -1;
		List<BeanStoreState> states = new ArrayList<>();
		
		final MutableEntityStoreSet store = new MutableEntityStoreSet();
		store.setAcceptNonGeneratedIds(true);
		
		PersistentTransactionConsumer ptc = new PersistentTransactionConsumer() {
			
			@Override
			public void accept(PersistentTransaction pt) {
				if (lastReadTransactionId == -1) {
					lastReadTransactionId = pt.getSeqNum();
					if (lastReadTransactionId != 1) {
						throw new RuntimeException("First transaction sequence num != 0");
					}
				} else {
					if (lastReadTransactionId  != (pt.getSeqNum() - 1)) {
						throw new RuntimeException("Transaction sequence broken " + lastReadTransactionId + " -> " + pt.getSeqNum());
					}
					lastReadTransactionId = pt.getSeqNum();
				}
				
				
				PersistentInstanceTransaction[] instanceTransactions = pt.getInstanceTransactions();
				if (instanceTransactions == null) {
					// normal transactions should not be empty
					if (pt.getTransactionType() == PersistentTransaction.TRANSACTION_TYPE_DEFAULT) {
						throw new RuntimeException("empty transaction");
					} else {
						instanceTransactions = new PersistentInstanceTransaction[0];
					}
				}

				for (PersistentInstanceTransaction pit : instanceTransactions) {
					@SuppressWarnings({ "rawtypes" })
					MutableEntityStore entityStore = (MutableEntityStore) store.storeOptional(pit.getAlias())
							.orElseGet(() -> {
								return (MutableEntityStore) store.register(CompanionRegistry.getOrCreateMapCompanion(pit.getAlias()));
							});

					AbstractPersistentObject instance = null;
					switch (pit.getType()) {
					case PersistentInstanceTransaction.TYPE_CREATE:
						instance = entityStore.companion().createInstance();
						instance.id(pit.getId());
						instance.state(State.PREPARE);
						entityStore.put(instance);
						
						break;
					case PersistentInstanceTransaction.TYPE_DELETE:
						if (entityStore.remove(pit.getId()) == null) {
							throw new AssertionError();
						}
						break;
					case PersistentInstanceTransaction.TYPE_UPDATE:
						instance = entityStore.get(pit.getId());
						if (instance == null) {
							throw new AssertionError();
						}
						break;
					default:
						throw new AssertionError();
					}

					// update fields
					switch (pit.getType()) {
					case PersistentInstanceTransaction.TYPE_CREATE:
					case PersistentInstanceTransaction.TYPE_UPDATE:
						instance.version(pit.getVersion());
						if (pit.getPropertyUpdates() != null) {
							for (PersistentProperty propertyUpdate : pit.getPropertyUpdates()) {
								if (propertyUpdate.getValue() instanceof ValueUpdateFunction<?>) {
									@SuppressWarnings("rawtypes")
									ValueUpdateFunction valueUpdateFunction = (ValueUpdateFunction) propertyUpdate.getValue();
									Object propValue = instance.get(propertyUpdate.getProperty());
									Object newValue = valueUpdateFunction.apply(propValue);
									instance.put(propertyUpdate.getProperty(), newValue);
								} else {
									instance.put(propertyUpdate.getProperty(), propertyUpdate.getValue());
								}	
							}
						}
					}

				}
				
				if (transactionListener != null) {
					transactionListener.accept(pt);
				}
				
				states.add(transactionToState(pt));
			}
			
			@Override
			public boolean wantsNextTransaction() {
				return state == null || lastReadTransactionId < state.intValue();
			}
		};
		
		reader.load(ptc);
		
		// set state of all instances to 'Stored'
		store.forEach(es -> {
			es.objects().forEach(instance -> instance.state(State.STORED));
		});
		
		store.version(lastReadTransactionId);
		
		store.reloadLinks();
		
		return new LoadedStoreData(persistence, store, states);
	}


	
	
}
