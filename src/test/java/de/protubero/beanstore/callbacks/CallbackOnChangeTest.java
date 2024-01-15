package de.protubero.beanstore.callbacks;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.persistence.impl.InMemoryPersistence;
import de.protubero.beanstore.tx.TransactionPhase;

public class CallbackOnChangeTest {

	
	@Test
	public void testOnChange() throws InterruptedException, ExecutionException {
		InMemoryPersistence persistence = InMemoryPersistence.create();
		var builder = BeanStoreBuilder.init(persistence);
		builder.registerMapEntity("user");
		var store = builder.build();
		
		CallbackInfo info = new CallbackInfo();
		
		
		store.callbacks().onChange(event -> {
			info.onCallWithThread(Thread.currentThread().getId());
			
			Assertions.assertEquals(event.getTargetStateVersion().intValue(), persistence.lastSeqNum());
			
			Assertions.assertEquals(1, event.getInstanceEvents().size());
			Assertions.assertEquals(TransactionPhase.COMMITTED_SYNC, event.phase());
		});
		
		store.locked(ctx -> {
			info.setTransactionsThreadId(Thread.currentThread().getId());
			
			var tx = ctx.transaction();
			var user = tx.create("user");
			user.put("name", "Mario");
			tx.execute();
			
		});
		
		Assertions.assertEquals(1, info.getCalledCount());
		Assertions.assertEquals(info.getTransactionsThreadId(), info.getCallbackThreadId());
	}
	
	@Test
	public void testOnChangeInstance() throws InterruptedException, ExecutionException {
		InMemoryPersistence persistence = InMemoryPersistence.create();
		var builder = BeanStoreBuilder.init(persistence);
		builder.registerMapEntity("user");
		var store = builder.build();
		
		CallbackInfo info = new CallbackInfo();
		
		
		store.callbacks().onChangeInstance(event -> {
			info.onCallWithThread(Thread.currentThread().getId());
						
			Assertions.assertEquals("user", event.entity().alias());
			Assertions.assertEquals(TransactionPhase.COMMITTED_SYNC, event.transactionEvent().phase());
		});
		
		store.locked(ctx -> {
			info.setTransactionsThreadId(Thread.currentThread().getId());
			
			var tx = ctx.transaction();
			var user = tx.create("user");
			user.put("name", "Mario");
			tx.execute();
			
		});
		
		Assertions.assertEquals(1, info.getCalledCount());
		Assertions.assertEquals(info.getTransactionsThreadId(), info.getCallbackThreadId());
	}

	@Test
	public void testOnChangeAsync() throws InterruptedException, ExecutionException {
		InMemoryPersistence persistence = InMemoryPersistence.create();
		var builder = BeanStoreBuilder.init(persistence);
		builder.registerMapEntity("user");
		var store = builder.build();
		
		CallbackInfo info = new CallbackInfo();
		
		
		store.callbacks().onChangeAsync(event -> {
			try {
				Thread.sleep(200l);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			info.onCallWithThread(Thread.currentThread().getId());
			
			Assertions.assertEquals(event.getTargetStateVersion().intValue(), persistence.lastSeqNum());
			
			Assertions.assertEquals(1, event.getInstanceEvents().size());
			Assertions.assertEquals(TransactionPhase.COMMITTED_ASYNC, event.phase());
		});
		
		store.locked(ctx -> {
			info.setTransactionsThreadId(Thread.currentThread().getId());
			
			var tx = ctx.transaction();
			var user = tx.create("user");
			user.put("name", "Mario");
			tx.execute();
			
		});
		
		Assertions.assertEquals(0, info.getCalledCount());
		Thread.sleep(500l);
		Assertions.assertEquals(1, info.getCalledCount());
		Assertions.assertNotEquals(info.getTransactionsThreadId(), info.getCallbackThreadId());
	}
	
	@Test
	public void testOnChangeInstanceAsync() throws InterruptedException, ExecutionException {
		InMemoryPersistence persistence = InMemoryPersistence.create();
		var builder = BeanStoreBuilder.init(persistence);
		builder.registerMapEntity("user");
		var store = builder.build();
		
		CallbackInfo info = new CallbackInfo();
		
		store.callbacks().onChangeInstanceAsync(event -> {
			try {
				Thread.sleep(200l);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			info.onCallWithThread(Thread.currentThread().getId());
						
			Assertions.assertEquals("user", event.entity().alias());
			Assertions.assertEquals(TransactionPhase.COMMITTED_ASYNC, event.transactionEvent().phase());
		});
		
		store.locked(ctx -> {
			info.setTransactionsThreadId(Thread.currentThread().getId());
			
			var tx = ctx.transaction();
			var user = tx.create("user");
			user.put("name", "Mario");
			tx.execute();
			
		});
		
		Assertions.assertEquals(0, info.getCalledCount());
		Thread.sleep(500l);
		Assertions.assertEquals(1, info.getCalledCount());
		Assertions.assertNotEquals(info.getTransactionsThreadId(), info.getCallbackThreadId());
	}
	
	
}
