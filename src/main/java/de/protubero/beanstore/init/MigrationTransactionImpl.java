package de.protubero.beanstore.init;

import de.protubero.beanstore.writer.Transaction;

public class MigrationTransactionImpl extends BaseTransactionImpl implements MigrationTransaction {

	public MigrationTransactionImpl(Transaction transaction) {
		super(transaction);
	}

}
