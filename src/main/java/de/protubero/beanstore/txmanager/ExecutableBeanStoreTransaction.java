package de.protubero.beanstore.txmanager;

import java.util.function.Consumer;

import de.protubero.beanstore.writer.BeanStoreChange;
import de.protubero.beanstore.writer.BeanStoreTransaction;

public interface ExecutableBeanStoreTransaction extends BeanStoreTransaction {

	default void executeAsync() {
		executeAsync(null);
	}

	void executeAsync(Consumer<BeanStoreChange> consumer);

	BeanStoreChange execute();
	
	
}
