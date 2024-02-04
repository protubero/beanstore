package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.EntityStoreSet;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStoreSet;
import de.protubero.beanstore.tx.StoreWriter;
import de.protubero.beanstore.tx.Transaction;
import de.protubero.beanstore.tx.TransactionPhase;
import de.protubero.beanstore.tx.TxUtil;

public class InterimStore {

	public static final Logger log = LoggerFactory.getLogger(InterimStore.class);

	
	public static enum Phase {
		MutableMaps, 
		ImmutableBeans
	}

	private Consumer<PersistentTransaction> transactionListener;

	private StoreWriter storeWriter;	
	private ListTransactionWriter cachingTransactionWriter = new ListTransactionWriter();
	private List<BeanStoreState> writtenStates = new ArrayList<>();
	private List<BeanStoreState> loadedStates;
	private TransactionPersistence persistence;
	private EntityStoreSet<?> store;
	private Phase phase = Phase.MutableMaps;
	
	
	private InterimStore(LoadedStoreData aStoreData, Consumer<PersistentTransaction> aTransactionListener) {
		this.loadedStates = aStoreData.states();
		this.persistence = aStoreData.persistence();
		this.store = aStoreData.store().orElse(null);
		
		this.transactionListener = aTransactionListener;
		
		persistence.onStartStoreBuild();
	
		storeWriter = new StoreWriter();

		storeWriter.registerSyncInternalTransactionListener(TransactionPhase.PERSIST, t -> {
			PersistentTransaction pTransaction = TxUtil.createPersistentTransaction(t);

			if (transactionListener != null) {
				transactionListener.accept(pTransaction);
			}
			
			cachingTransactionWriter.append(pTransaction);
			
			BeanStoreState state = new BeanStoreState(
					pTransaction.getTransactionId(), 
					pTransaction.getTimestamp(), 
					pTransaction.getTransactionType(),
					pTransaction.getSeqNum()
					);
			writtenStates.add(state);
		});
		
	}
	
	public static InterimStore of(LoadedStoreData storeData, Consumer<PersistentTransaction> aPersistenceWriteListener) {
		return new InterimStore(storeData, aPersistenceWriteListener);
	}

	public static InterimStore of(LoadedStoreData storeData) {
		return new InterimStore(storeData, null);
	}

	public CompanionSet companionSet() {
		return store.companionsShip();
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

	public void setStore(EntityStoreSet<?> store) {
		if (store instanceof MutableEntityStoreSet) {
			if (phase == Phase.ImmutableBeans) {
				throw new AssertionError();
			}
		}

		if (store instanceof ImmutableEntityStoreSet) {
			phase = Phase.ImmutableBeans;
		}
		
		this.store = store;
	}

	public Phase getPhase() {
		return phase;
	}
	
	public List<String> appliedMigrationIds() {
		List<String> appliedMigrations = loadedStates.stream()
		.filter(state -> state.getTransactionType() == PersistentTransaction.TRANSACTION_TYPE_MIGRATION)
		.map(state -> state.migrationId().get())
		.collect(Collectors.toList());
		return appliedMigrations;
	}
	

	public BeanStore build() {
		if (phase != Phase.ImmutableBeans) {
			throw new AssertionError();
		}
		
		// persist migration transactions
		// this is the first time that data gets written to the file
		cachingTransactionWriter.dump(persistence.writer());

		return BuildUtil.build((ImmutableEntityStoreSet) store, persistence.writer());
	}
	
}
