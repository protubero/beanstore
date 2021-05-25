package de.protubero.beanstore.txmanager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import de.protubero.beanstore.writer.TransactionEvent;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;

class ThreadPoolTransactionManager extends AbstractTransactionManager {

	
	
	private ExecutorService executor;

	/**
	 * @param storeWriter
	 */
	public ThreadPoolTransactionManager(StoreWriter storeWriter, int threadPool) {
		super(storeWriter);
		
		executor = Executors.newFixedThreadPool(threadPool);		
	}

	@Override
	public void executeAsync(Transaction transaction, Consumer<TransactionEvent> consumer) {
		TransactionEvent result;
		try {
			result = executor.submit(() -> {
				return execute(transaction);			
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		if (consumer != null) {
			consumer.accept(result);
		}
	}
	
	@Override
	public TransactionEvent execute(Transaction transaction) {
		storeWriter.execute(transaction);
		return transaction;
	}

	@Override
	public void close() {
		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void executeDeferred(Consumer<TransactionFactory> consumer) {
		synchronized(storeWriter) {
			immediate(consumer);			
		};
	}


	@Override
	public void executeDeferredAsync(Consumer<TransactionFactory> consumer) {
		executor.submit(() -> {
			executeDeferred(consumer);			
		});
	}


}