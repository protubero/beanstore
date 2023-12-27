package de.protubero.beanstore.impl;

import java.util.Objects;

import de.protubero.beanstore.api.BaseTransaction;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.writer.Transaction;

public class BaseTransactionImpl implements BaseTransaction {

	protected Transaction transaction;
	
	public BaseTransactionImpl(Transaction transaction) {
		this.transaction = Objects.requireNonNull(transaction);
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


	@Override
	public <T extends AbstractPersistentObject> T create(T instance) {
		return transaction.create(instance);
	}


}
