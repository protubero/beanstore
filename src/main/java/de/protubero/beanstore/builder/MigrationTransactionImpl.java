package de.protubero.beanstore.builder;

import java.util.Objects;

import de.protubero.beanstore.api.BeanStoreSnapshot;
import de.protubero.beanstore.entity.Keys;
import de.protubero.beanstore.entity.MapObject;
import de.protubero.beanstore.tx.Transaction;

public class MigrationTransactionImpl implements MigrationTransaction {

	private BeanStoreSnapshot state;
	protected Transaction transaction;
	
	public MigrationTransactionImpl(Transaction transaction, BeanStoreSnapshot state) {
		this.transaction = Objects.requireNonNull(transaction);
		this.state = Objects.requireNonNull(state);
	}

	@Override
	public BeanStoreSnapshot snapshot() {
		return state;
	}

	@Override
	public MapObject create(String alias) {
		return (MapObject) transaction.create(alias);
	}

	@Override
	public MapObject update(MapObject instance) {
		return transaction.update(Keys.key(instance));
	}

	@Override
	public MapObject update(String alias, long id) {
		return (MapObject) transaction.update(Keys.key(alias, id));
	}

	@Override
	public void delete(String alias, long id) {
		transaction.delete(Keys.key(alias, id));
	}

	@Override
	public void delete(MapObject instance) {
		transaction.delete(Keys.key(instance));
	}

}
