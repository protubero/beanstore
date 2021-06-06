package de.protubero.beanstore;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import de.protubero.beanstore.api.BeanStorePlugin;
import de.protubero.beanstore.persistence.base.PersistentTransaction;

public class EmptyTransactionTest extends AbstractBeanStoreTest {

	private boolean transactionNotification;
	private boolean transactionPersisted;
	
	@Test
	public void test() {
		var store = addSampleData(createEmptyStore(new BeanStorePlugin() {
			@Override
			public void onWriteTransaction(PersistentTransaction transaction) {
				transactionPersisted = true;
			}
		}));
		
		transactionPersisted = false;
		
		store.callbacks().onChange(te -> {
			transactionNotification = true;
		});
		store.callbacks().onChangeInstance(ite -> {
			transactionNotification = true;
		});
		
		var txEvent = store.transaction().execute();
		assertTrue(txEvent.success());
		assertEquals(0, txEvent.getInstanceEvents().size());
		
		store.locked(txf -> {
			var txEvent2 = txf.transaction().execute();
			assertTrue(txEvent2.success());
			assertEquals(0, txEvent2.getInstanceEvents().size());
		});

		assertFalse(transactionNotification);
		assertFalse(transactionPersisted);
	}
	
}
