package de.protubero.beanstore.impl;

import de.protubero.beanstore.api.MigrationTransaction;
import de.protubero.beanstore.writer.Transaction;

public class MigrationTransactionImpl extends BaseTransactionImpl implements MigrationTransaction {

	public MigrationTransactionImpl(Transaction transaction) {
		super(transaction);
	}

}
