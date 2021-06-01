package de.protubero.beanstore.init;

import de.protubero.beanstore.base.AbstractEntity;
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

	
}
