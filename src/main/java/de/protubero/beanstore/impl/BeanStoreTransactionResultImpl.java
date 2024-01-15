package de.protubero.beanstore.impl;

import java.util.List;
import java.util.Objects;

import de.protubero.beanstore.api.BeanStoreSnapshot;
import de.protubero.beanstore.api.BeanStoreTransactionResult;
import de.protubero.beanstore.tx.InstanceTransactionEvent;
import de.protubero.beanstore.tx.TransactionEvent;
import de.protubero.beanstore.tx.TransactionFailure;
import de.protubero.beanstore.tx.TransactionPhase;

public class BeanStoreTransactionResultImpl implements BeanStoreTransactionResult {

	private TransactionEvent baseResult;
	private BeanStoreSnapshot baseStoreState;
	private BeanStoreSnapshot resultStoreState;

	public BeanStoreTransactionResultImpl(TransactionEvent baseResult, BeanStoreSnapshot baseStoreState,
			BeanStoreSnapshot resultStoreState) {
		this.baseResult = Objects.requireNonNull(baseResult);
		this.baseStoreState = Objects.requireNonNull(baseStoreState);
		this.resultStoreState = Objects.requireNonNull(resultStoreState);
	}

	
	@Override
	public List<? extends InstanceTransactionEvent<?>> getInstanceEvents() {
		return baseResult.getInstanceEvents();
	}

	@Override
	public boolean failed() {
		return baseResult.failed();
	}

	@Override
	public TransactionFailure exception() {
		return baseResult.failure();
	}

	@Override
	public TransactionPhase phase() {
		return baseResult.phase();
	}

	@Override
	public BeanStoreSnapshot baseStoreState() {
		return baseStoreState;
	}

	@Override
	public BeanStoreSnapshot resultStoreState() {
		return resultStoreState;
	}

}
