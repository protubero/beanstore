package de.protubero.beanstore.init;

import java.util.Objects;

import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.store.BeanStoreReadAccess;
import de.protubero.beanstore.writer.Transaction;

public class BaseTransactionImpl implements BaseTransaction {

	protected Transaction transaction;
	
	public BaseTransactionImpl(Transaction transaction) {
		this.transaction = Objects.requireNonNull(transaction);
	}

	@Override
	public BeanStoreReadAccess read() {
		return new BeanStoreReadAccessImpl(transaction.store());
	}

	@Override
	public <T extends AbstractPersistentObject> T create(String alias) {
		return transaction.create(alias);
	}

	@Override
	public <T extends AbstractPersistentObject> T update(T instance) {
		return transaction.update(instance);
	}

	@Override
	public <T extends AbstractPersistentObject> void delete(String alias, long id) {
		transaction.delete(alias, id);
	}

	@Override
	public <T extends AbstractPersistentObject> void delete(T instance) {
		transaction.delete(instance);
	}

}
