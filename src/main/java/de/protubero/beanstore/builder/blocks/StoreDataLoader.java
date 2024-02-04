package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.builder.blocks.InterimStore.Phase;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.entity.AbstractPersistentObject.State;
import de.protubero.beanstore.impl.BeanStoreImpl;
import de.protubero.beanstore.entity.MapObjectCompanion;
import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.api.PersistentProperty;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.store.CompanionSetImpl;
import de.protubero.beanstore.store.ImmutableEntityStoreBase;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;

public final class StoreDataLoader implements Supplier<LoadedStoreData> {

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
	
	@Override
	public LoadedStoreData get() {
		if (persistence.isEmpty()) {
			return new LoadedStoreData(persistence, null, Collections.emptyList());
		} else {
			return load(persistence.reader()); 
		}
	}	
	
	@SuppressWarnings("unchecked")
	private LoadedStoreData load(TransactionReader reader) {
		lastReadTransactionId = -1;
		List<BeanStoreState> states = new ArrayList<>();
		
		final MutableEntityStoreSet store = new MutableEntityStoreSet();
		store.setAcceptNonGeneratedIds(true);
		
		reader.load(pt -> {
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
				if (pt.getTransactionId() == null) {
					throw new RuntimeException("empty transaction");
				} else {
					instanceTransactions = new PersistentInstanceTransaction[0];
				}
			}

			for (PersistentInstanceTransaction pit : instanceTransactions) {
				@SuppressWarnings({ "rawtypes" })
				MutableEntityStore entityStore = (MutableEntityStore) store.storeOptional(pit.getAlias())
						.orElseGet(() -> {
							return (MutableEntityStore) store.register(new MapObjectCompanion(pit.getAlias()));
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
							instance.put(propertyUpdate.getProperty(), propertyUpdate.getValue());
						}
					}
				}

			}
			
			if (transactionListener != null) {
				transactionListener.accept(pt);
			}
			
			BeanStoreState state = new BeanStoreState(
					pt.getTransactionId(), 
					pt.getTimestamp(), 
					pt.getTransactionType(),
					pt.getSeqNum()
					);
			states.add(state);
		});
		
		// set state of all instances to 'Stored'
		store.forEach(es -> {
			es.objects().forEach(instance -> instance.state(State.STORED));
		});
		
		store.version(lastReadTransactionId);
		
		return new LoadedStoreData(persistence, store, states);
	}

	
	
}
