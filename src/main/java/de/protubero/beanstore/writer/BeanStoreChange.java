package de.protubero.beanstore.writer;

import java.util.List;

import de.protubero.beanstore.base.BeanChange;
import de.protubero.beanstore.writer.Transaction.TransactionPhase;

public interface BeanStoreChange {
	
	List<BeanChange<?>> getBeanChanges();
	
	default boolean success() {
		return !failed();
	}
	
	boolean failed();
	
	TransactionFailure exception();
	
	TransactionPhase phase();
	
	
}
