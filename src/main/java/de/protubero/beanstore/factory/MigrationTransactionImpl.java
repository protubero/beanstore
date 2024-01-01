package de.protubero.beanstore.factory;

import java.util.Objects;

import de.protubero.beanstore.api.BeanStoreState;
import de.protubero.beanstore.entity.MapObject;
import de.protubero.beanstore.impl.BaseTransactionImpl;
import de.protubero.beanstore.tx.Transaction;

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

	@SuppressWarnings("unchecked")
	@Override
	public MapObject create(String alias) {
		return (MapObject) transaction.create(alias);
	}

	@Override
	public MapObject update(MapObject instance) {
		return transaction.update(instance);
	}

	@Override
	public MapObject update(String alias, long id) {
		return transaction.updateMapObject(alias, id);
	}

	@Override
	public void delete(String alias, long id) {
		transaction.delete(alias, id);
	}

	@Override
	public void delete(MapObject instance) {
		transaction.delete(instance);
	}

}
