package de.protubero.beanstore.impl;

import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.tx.Transaction;

public class BeanStoreTransactionImpl extends BaseTransactionImpl implements BeanStoreTransaction {

	public BeanStoreTransactionImpl(Transaction transaction) {
		super(transaction);
	}

	@Override
	public <T extends AbstractEntity> T create(Class<T> aClass) {
		return transaction.create(aClass);
	}

	@Override
	public <T extends AbstractEntity> void delete(Class<T> aClass, long id) {
		transaction.delete(aClass, id);
	}


	@Override
	public <T extends AbstractPersistentObject> void deleteOptLocked(String alias, long id, int version) {
		transaction.deleteOptLocked(alias, id, version);
	}

	@Override
	public <T extends AbstractEntity> void deleteOptLocked(Class<T> aClass, long id, int version) {
		transaction.deleteOptLocked(aClass, id, version);
	}

	@Override
	public <T extends AbstractPersistentObject> void deleteOptLocked(T instance) {
		transaction.deleteOptLocked(instance);
	}

	@Override
	public <T extends AbstractPersistentObject> T updateOptLocked(T instance) {
		return transaction.updateOptLocked(instance);
	}

	@Override
	public <T extends AbstractEntity> T updateOptLocked(Class<T> aClass, long id, int version) {
		return transaction.updateOptLocked(aClass, id, version);
	}

	@Override
	public <T extends AbstractEntity> T update(Class<T> aClass, long id) {
		return transaction.update(aClass, id);
	}

	@Override
	public void describe(String text) {
		transaction.setDescription(text);
	}

	
}
