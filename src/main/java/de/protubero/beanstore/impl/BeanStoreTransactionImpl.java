package de.protubero.beanstore.impl;

import java.util.Objects;

import de.protubero.beanstore.api.BeanStoreTransaction;
import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.entity.PersistentObjectVersionKey;
import de.protubero.beanstore.links.Link;
import de.protubero.beanstore.tx.Transaction;

public class BeanStoreTransactionImpl implements BeanStoreTransaction {

	protected Transaction transaction;

	public BeanStoreTransactionImpl(Transaction transaction) {
		this.transaction = Objects.requireNonNull(transaction);
	}

	@Override
	public AbstractPersistentObject create(String alias) {
		return transaction.create(alias);
	}
	

	@Override
	public <T extends AbstractPersistentObject> T create(T instance) {
		return transaction.create(instance);
	}

	@Override
	public <T extends AbstractEntity> T create(Class<T> aClass) {
		return transaction.create(aClass);
	}

	@Override
	public void describe(String text) {
		transaction.setDescription(text);
	}

	@Override
	public void delete(PersistentObjectKey<?> key) {
		transaction.delete(key);
	}

	@Override
	public void delete(PersistentObjectVersionKey<?> key) {
		transaction.delete(key);
	}

	@Override
	public <T extends AbstractPersistentObject> T update(PersistentObjectKey<T> key) {
		return transaction.update(key);
	}

	@Override
	public <T extends AbstractPersistentObject> T update(PersistentObjectVersionKey<T> key) {
		return transaction.update(key);
	}

	@Override
	public <S extends AbstractPersistentObject, T extends AbstractPersistentObject> void delete(Link<S, T> link) {
		transaction.delete(link);
	}

	@Override
	public <S extends AbstractPersistentObject, T extends AbstractPersistentObject> void link(
			PersistentObjectKey<S> sourceKey, PersistentObjectKey<T> targetKey, String type) {
		transaction.link(sourceKey, targetKey, type);
	}

	@Override
	public <S extends AbstractPersistentObject, T extends AbstractPersistentObject> void link(S sourceObj, T targetObj,
			String type) {
		transaction.link(sourceObj, targetObj, type);
	}

	@Override
	public void delete(PersistentObjectKey<?> key, boolean ignoreNonExistence) {
		transaction.delete(key, ignoreNonExistence);
	}

	
}
