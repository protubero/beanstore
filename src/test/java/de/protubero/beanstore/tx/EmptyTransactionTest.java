package de.protubero.beanstore.tx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.AbstractBeanStoreTest;
import de.protubero.beanstore.api.BeanStoreTransactionResult;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.pluginapi.PersistenceWriteListener;

public class EmptyTransactionTest extends AbstractBeanStoreTest {

	private boolean transactionNotification;
	private boolean transactionPersisted;
	
	@TempDir
	File pFileDir;
	
	
	
	@Override
	protected File getFileDir() {
		return pFileDir;
	}
	
	@Test
	public void test() throws InterruptedException, ExecutionException {
		var store = addSampleData(createEmptyStore(new PersistenceWriteListener() {
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
		
		var txEvent = store.transaction().executeAsync().get();
		assertTrue(txEvent.success());
		assertEquals(0, txEvent.getInstanceEvents().size());
		
		store.locked(txf -> {
			BeanStoreTransactionResult txEvent2;
			try {
				txEvent2 = txf.get().executeAsync().get();
				assertTrue(txEvent2.success());
				assertEquals(0, txEvent2.getInstanceEvents().size());
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException();
			}
		});

		assertFalse(transactionNotification);
		assertFalse(transactionPersisted);
	}
	
}
