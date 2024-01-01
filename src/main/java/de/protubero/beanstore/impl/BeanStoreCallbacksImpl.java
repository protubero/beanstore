package de.protubero.beanstore.impl;

import java.util.Objects;
import java.util.function.Consumer;

import de.protubero.beanstore.api.BeanStoreCallbacks;
import de.protubero.beanstore.tx.InstanceTransactionEvent;
import de.protubero.beanstore.tx.StoreWriter;
import de.protubero.beanstore.tx.TransactionEvent;

public class BeanStoreCallbacksImpl implements BeanStoreCallbacks {

	private StoreWriter storeWriter;
	
	BeanStoreCallbacksImpl(StoreWriter storeWriter) {
		this.storeWriter = Objects.requireNonNull(storeWriter);
	}

	@Override
	public void verify(Consumer<TransactionEvent> consumer) {
		storeWriter.verify(consumer);
	}

	@Override
	public void verifyInstance(Consumer<InstanceTransactionEvent<?>> consumer) {
		storeWriter.verifyInstance(consumer);
	}

	@Override
	public void onChange(Consumer<TransactionEvent> consumer) {
		storeWriter.onChange(consumer);
	}

	@Override
	public void onChangeInstance(Consumer<InstanceTransactionEvent<?>> consumer) {
		storeWriter.onChangeInstance(consumer);
	}

	@Override
	public void onChangeAsync(Consumer<TransactionEvent> consumer) {
		storeWriter.onChangeAsync(consumer);
	}

	@Override
	public void onChangeInstanceAsync(Consumer<InstanceTransactionEvent<?>> consumer) {
		storeWriter.onChangeInstanceAsync(consumer);
	}

}
