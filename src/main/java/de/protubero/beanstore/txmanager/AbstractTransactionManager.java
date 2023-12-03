package de.protubero.beanstore.txmanager;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.protubero.beanstore.base.tx.TransactionEvent;
import de.protubero.beanstore.store.CompanionShip;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;
import de.protubero.beanstore.writer.TransactionStoreContext;
import de.protubero.beanstore.writer.TransactionListener;

public abstract class AbstractTransactionManager implements TransactionManager {

	protected TransactionStoreContext context;
	
	public AbstractTransactionManager(
			TransactionStoreContext context) {
		this.context = Objects.requireNonNull(context);
	}
	
	public TransactionStoreContext context() {
		return context;
	}
	
	protected void immediate(Consumer<TransactionFactory> consumer) {
		consumer.accept(new LockedStoreTransactionFactory(context));
	}
	
	

}
