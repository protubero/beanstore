package de.protubero.beanstore.callbacks;

import de.protubero.beanstore.api.BeanStoreTransactionResult;

public class CallbackInfo {
	private int calledCount;
	private Long callbackThreadId;
	private Long transactionsThreadId;
	private BeanStoreTransactionResult transactionResult;

	public int getCalledCount() {
		return calledCount;
	}
	
	public void incCallCount() {
		calledCount++;
	}

	public void setCallbackThreadId(long aThreadId) {
		this.callbackThreadId = aThreadId;
	}

	public Long getCallbackThreadId() {
		return callbackThreadId;
	}

	public void onCallWithThread(long aThreadId) {
		incCallCount();
		setCallbackThreadId(aThreadId);
	}

	public Long getTransactionsThreadId() {
		return transactionsThreadId;
	}

	public void setTransactionsThreadId(long aTransactionsThreadId) {
		this.transactionsThreadId = aTransactionsThreadId;
	}


	public void setTransactionResult(BeanStoreTransactionResult aTransactionResult) {
		this.transactionResult = aTransactionResult;
	}

	public BeanStoreTransactionResult getTransactionResult() {
		return transactionResult;
	}
}