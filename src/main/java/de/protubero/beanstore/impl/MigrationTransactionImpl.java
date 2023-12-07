package de.protubero.beanstore.impl;

import java.util.Objects;

import de.protubero.beanstore.api.BeanStoreState;
import de.protubero.beanstore.api.MigrationTransaction;
import de.protubero.beanstore.writer.Transaction;

public class MigrationTransactionImpl extends BaseTransactionImpl implements MigrationTransaction {

	private BeanStoreState state;
	
	public MigrationTransactionImpl(Transaction transaction, BeanStoreState state) {
		super(transaction);
		
		this.state = Objects.requireNonNull(state);
	}

	@Override
	public BeanStoreState state() {
		return state;
	}

}
