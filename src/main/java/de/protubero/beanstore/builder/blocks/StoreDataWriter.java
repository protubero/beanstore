package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.impl.BeanStoreImpl;
import de.protubero.beanstore.persistence.api.DeferredTransactionWriter;
import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.api.PersistentProperty;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.EntityStoreSet;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.tx.InstanceTransactionEvent;
import de.protubero.beanstore.tx.StoreWriter;
import de.protubero.beanstore.tx.Transaction;
import de.protubero.beanstore.tx.TransactionPhase;

public class StoreDataWriter {

	public static final Logger log = LoggerFactory.getLogger(StoreDataWriter.class);

	
	public static enum Phase {
		MutableMaps, 
		ImmutableBeans
	}

	private Consumer<PersistentTransaction> transactionListener;

	private StoreWriter storeWriter;	
	private DeferredTransactionWriter deferredTransactionWriter;
	private List<BeanStoreState> writtenStates = new ArrayList<>();
	private List<BeanStoreState> loadedStates;
	private TransactionPersistence persistence;
	private EntityStoreSet<?> store;
	private Phase phase = Phase.MutableMaps;
	
	
	private StoreDataWriter(LoadedStoreData aStoreData, Consumer<PersistentTransaction> aTransactionListener) {
		this.loadedStates = aStoreData.states();
		this.persistence = aStoreData.persistence();
		this.store = aStoreData.store().orElse(null);
		
		this.transactionListener = aTransactionListener;
		
		persistence.onStartStoreBuild();
		deferredTransactionWriter = new DeferredTransactionWriter(persistence.writer());
	
		storeWriter = new StoreWriter();

		storeWriter.registerSyncInternalTransactionListener(TransactionPhase.PERSIST, t -> {
			PersistentTransaction pTransaction = createTransaction(t);

			if (transactionListener != null) {
				transactionListener.accept(pTransaction);
			}
			
			deferredTransactionWriter.append(pTransaction);
			
			BeanStoreState state = new BeanStoreState(
					pTransaction.getTransactionId(), 
					pTransaction.getTimestamp(), 
					pTransaction.getTransactionType(),
					pTransaction.getSeqNum()
					);
			writtenStates.add(state);
		});
		
	}
	
	public static StoreDataWriter of(LoadedStoreData storeData, Consumer<PersistentTransaction> aPersistenceWriteListener) {
		return new StoreDataWriter(storeData, aPersistenceWriteListener);
	}

	public static StoreDataWriter of(LoadedStoreData storeData) {
		return new StoreDataWriter(storeData, null);
	}

	public CompanionSet companionSet() {
		return store.companionsShip();
	}
	
	private PersistentTransaction createTransaction(Transaction transaction) {
		PersistentTransaction pt = new PersistentTransaction(transaction.getTransactionType(), transaction.getTransactionId());		
		pt.setTimestamp(Objects.requireNonNull(transaction.getTimestamp()));
		if (transaction.getTargetStateVersion() == null) {
			throw new AssertionError();
		}
		pt.setSeqNum(transaction.getTargetStateVersion().intValue());
		
		PersistentInstanceTransaction[] eventArray = new PersistentInstanceTransaction[transaction.getInstanceEvents().size()];
		int idx = 0;
		for (InstanceTransactionEvent<?> event : transaction.getInstanceEvents()) {
			PersistentInstanceTransaction pit = new PersistentInstanceTransaction();
			eventArray[idx++] = pit;
			pit.setAlias(event.entity().alias());
			switch (event.type()) {
			case Delete:
				pit.setType(PersistentInstanceTransaction.TYPE_DELETE);
				pit.setId(event.replacedInstance().id());
				pit.setVersion(event.replacedInstance().version());
				break;
			case Update:
				pit.setType(PersistentInstanceTransaction.TYPE_UPDATE);
				pit.setId(event.newInstance().id());
				pit.setPropertyUpdates((PersistentProperty[]) event.values());
				pit.setVersion(event.newInstance().version());
				
				break;
			case Create:
				pit.setType(PersistentInstanceTransaction.TYPE_CREATE);
				pit.setId(event.newInstance().id());
				pit.setPropertyUpdates((PersistentProperty[]) event.values());
				pit.setVersion(event.newInstance().version());

				break;
			}
		}
		pt.setInstanceTransactions(eventArray);
		
		return pt;
	}

	public StoreWriter getStoreWriter() {
		return storeWriter;
	}

	public List<BeanStoreState> getWrittenStates() {
		return Collections.unmodifiableList(writtenStates);
	}

	public List<BeanStoreState> getLoadedStates() {
		return loadedStates;
	}

	public EntityStoreSet<?> getStore() {
		return store;
	}

	public void execute(Transaction tx) {
		store = storeWriter.execute(tx, store);
	}

	public DeferredTransactionWriter getDeferredTransactionWriter() {
		return deferredTransactionWriter;
	}

	public void setStore(EntityStoreSet<?> store) {
		this.store = store;
	}

	public Phase getPhase() {
		return phase;
	}
	
	public void switchToImmutableBeansPhase() {
		if (phase != Phase.MutableMaps) {
			throw new AssertionError();
		}
		
		phase = Phase.ImmutableBeans;
	}

	public BeanStore build() {
		// persist migration transactions
		// this is the first time that data gets written to the file
		deferredTransactionWriter.switchToNonDeferred();

		Runnable onCloseStoreAction = () -> {try {
			log.info("Closing transaction writer");
			deferredTransactionWriter.close();
		} catch (Exception e) {
			log.error("Error closing transaction writer", e);
		}};			
		
		BeanStoreImpl beanStoreImpl = new BeanStoreImpl((ImmutableEntityStoreSet) store, onCloseStoreAction, storeWriter);
		return beanStoreImpl;
	}
	
}
