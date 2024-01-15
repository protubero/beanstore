package de.protubero.beanstore.callbacks;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.persistence.impl.InMemoryPersistence;
import de.protubero.beanstore.tx.TransactionFailure;
import de.protubero.beanstore.tx.TransactionFailureType;
import de.protubero.beanstore.tx.TransactionPhase;

public class CallbackVerifyTest {

	
	@Test
	public void testVerifySuccess() throws InterruptedException, ExecutionException {
		InMemoryPersistence persistence = InMemoryPersistence.create();
		var builder = BeanStoreBuilder.init(persistence);
		builder.registerMapEntity("user");
		var store = builder.build();
		
		CallbackInfo info = new CallbackInfo();
		
		store.callbacks().verify(event -> {
			info.onCallWithThread(Thread.currentThread().getId());
			
			Assertions.assertEquals(1, event.getInstanceEvents().size());
			Assertions.assertEquals(TransactionPhase.VERIFICATION, event.phase());
		});
		
		
		int initialVersion = store.snapshot().version();
		store.locked(ctx -> {
			info.setTransactionsThreadId(Thread.currentThread().getId());
			
			var tx = ctx.transaction();
			var user = tx.create("user");
			user.put("name", "Mario");
			info.setTransactionResult(tx.execute());
		});
		
		
		Assertions.assertEquals(1, info.getCalledCount());
		Assertions.assertEquals(info.getTransactionsThreadId(), info.getCallbackThreadId());
		Assertions.assertTrue(info.getTransactionResult().success());
		
		Assertions.assertEquals(store.snapshot().version(), initialVersion + 1);
		Assertions.assertEquals(store.snapshot().version(), persistence.lastSeqNum());
	}
	
	@Test
	public void testVerifyFailure() throws InterruptedException, ExecutionException {
		InMemoryPersistence persistence = InMemoryPersistence.create();
		var builder = BeanStoreBuilder.init(persistence);
		builder.registerMapEntity("user");
		var store = builder.build();
		
		CallbackInfo info = new CallbackInfo();
		
		store.callbacks().verify(event -> {
			info.onCallWithThread(Thread.currentThread().getId());
			
			Assertions.assertEquals(1, event.getInstanceEvents().size());
			Assertions.assertEquals(TransactionPhase.VERIFICATION, event.phase());
			
			throw new RuntimeException("Verification failed");
		});
		
		
		int initialVersion = store.snapshot().version();
		TransactionFailure failure = Assertions.assertThrows(TransactionFailure.class, () -> store.locked(ctx -> {
			info.setTransactionsThreadId(Thread.currentThread().getId());
			
			var tx = ctx.transaction();
			var user = tx.create("user");
			user.put("name", "Mario");
			info.setTransactionResult(tx.execute());
		}));
		
		Assertions.assertEquals(TransactionFailureType.VERIFICATION_FAILED, failure.getType());
		
		
		Assertions.assertEquals(1, info.getCalledCount());
		Assertions.assertEquals(info.getTransactionsThreadId(), info.getCallbackThreadId());
		
		Assertions.assertEquals(store.snapshot().version(), initialVersion);
		Assertions.assertEquals(store.snapshot().version(), persistence.lastSeqNum());
		
	}
	
}
