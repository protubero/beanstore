package de.protubero.beanstore.builder.blocks;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.protubero.beanstore.persistence.api.DeferredTransactionWriter;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.MutableEntityStoreSet;
import de.protubero.beanstore.tx.StoreWriter;

public class LoadedStoreData {

	private TransactionPersistence persistence;
	private MutableEntityStoreSet store;
	private List<BeanStoreState> states;

	LoadedStoreData(TransactionPersistence aPersistence, 
			MutableEntityStoreSet aStore,
			List<BeanStoreState> aStates) {
		this.persistence = Objects.requireNonNull(aPersistence);
		this.store = aStore;
		this.states = Objects.requireNonNull(aStates);
		
		if (store == null && states.size() > 0) {
			throw new AssertionError();
		}
		if (store != null && states.size() == 0) {
			throw new AssertionError();
		}
		
	}
	
	public Optional<MutableEntityStoreSet> store() {
		return Optional.ofNullable(store);
	}
	
	public List<BeanStoreState> states() {
		return Collections.unmodifiableList(states);
	}
	
	public CompanionSet companionSet() {
		return store.companionsShip();
	}
		
	public TransactionPersistence persistence() {
		return persistence;
	}
	
}
