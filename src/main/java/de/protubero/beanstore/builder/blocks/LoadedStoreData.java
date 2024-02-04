package de.protubero.beanstore.builder.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.Companion;
import de.protubero.beanstore.impl.BeanStoreImpl;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.store.CompanionSet;
import de.protubero.beanstore.store.CompanionSetImpl;
import de.protubero.beanstore.store.ImmutableEntityStoreBase;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.store.MutableEntityStore;
import de.protubero.beanstore.store.MutableEntityStoreSet;
import de.protubero.beanstore.tx.StoreWriter;
import de.protubero.beanstore.tx.TransactionPhase;
import de.protubero.beanstore.tx.TxUtil;

public class LoadedStoreData {

	public static final Logger log = LoggerFactory.getLogger(LoadedStoreData.class);
	
	
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
	
	@SuppressWarnings("unchecked")
	public BeanStore build(Consumer<PersistentTransaction> aTransactionListener) {
		ImmutableEntityStoreSet finalStoreSet = null;
		
		if (store == null) {
			// i.e. either no file set or file does not exist
			finalStoreSet = new ImmutableEntityStoreSet(new CompanionSetImpl(), 0);
		} else {
			List<ImmutableEntityStoreBase<?>> entityStoreBaseList = new ArrayList<>();
			
			// 1. iterate over loaded entities
			for (MutableEntityStore<?> es : store) {
				ImmutableEntityStoreBase<AbstractPersistentObject> newEntityStore = new ImmutableEntityStoreBase<>();
				entityStoreBaseList.add(newEntityStore);
				newEntityStore.setNextInstanceId(es.getNextInstanceId());
				newEntityStore.setCompanion((Companion<AbstractPersistentObject>) es.companion());
				newEntityStore.setObjectMap((Map<Long, AbstractPersistentObject>) es.getObjectMap());
			}	

			
			// Create final store set
			finalStoreSet = 
					new ImmutableEntityStoreSet(
							entityStoreBaseList.toArray(new ImmutableEntityStoreBase[entityStoreBaseList.size()]), store.version());
		}
		
		
		return BuildUtil.build(finalStoreSet, persistence.writer(), aTransactionListener );
	}
	
	
}
