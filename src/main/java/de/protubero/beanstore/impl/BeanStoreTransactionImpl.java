package de.protubero.beanstore.impl;

import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.writer.Transaction;

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
	public void create(AbstractPersistentObject apo) {
		transaction.create(apo);		
	}

	
}
