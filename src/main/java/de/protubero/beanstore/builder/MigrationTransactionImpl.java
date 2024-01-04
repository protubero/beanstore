package de.protubero.beanstore.builder;

import java.util.Objects;

import de.protubero.beanstore.api.BeanStoreSnapshot;
import de.protubero.beanstore.entity.MapObject;
import de.protubero.beanstore.impl.BaseTransactionImpl;
import de.protubero.beanstore.tx.Transaction;

public class MigrationTransactionImpl extends BaseTransactionImpl implements MigrationTransaction {

	private BeanStoreSnapshot state;
	
	public MigrationTransactionImpl(Transaction transaction, BeanStoreSnapshot state) {
		super(transaction);
		
		this.state = Objects.requireNonNull(state);
	}

	@Override
	public BeanStoreSnapshot state() {
		return state;
	}

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
