package de.protubero.beanstore.init;

import java.util.function.Consumer;

import de.protubero.beanstore.base.AbstractEntity;
import de.protubero.beanstore.base.AbstractPersistentObject;
import de.protubero.beanstore.store.BeanStoreReadAccess;
import de.protubero.beanstore.txmanager.TransactionManager;
import de.protubero.beanstore.writer.Transaction;
import de.protubero.beanstore.writer.TransactionEvent;

public class ExecutableTransaction  implements ExecutableBeanStoreTransaction {

	private TransactionManager executionManager;
	private Transaction transaction;
	
	public ExecutableTransaction(Transaction transaction, TransactionManager executionManager) {
		this.executionManager = executionManager;
		this.transaction = transaction;
	}

	@Override
	public void executeAsync(Consumer<TransactionEvent> consumer) {
		executionManager.executeAsync(transaction, consumer);
	}

	@Override
	public TransactionEvent execute() {
		return executionManager.execute(transaction);
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
	public BeanStoreReadAccess read() {
		return new BeanStoreReadAccessImpl(transaction.store());
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

}
