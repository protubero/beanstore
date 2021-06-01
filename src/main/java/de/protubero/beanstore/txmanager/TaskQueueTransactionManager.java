package de.protubero.beanstore.txmanager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.base.GenericWrapper;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;
import de.protubero.beanstore.writer.TransactionEvent;

public class TaskQueueTransactionManager extends AbstractTransactionManager {

	public static final Logger log = LoggerFactory.getLogger(TaskQueueTransactionManager.class);

	
	private BlockingQueue<Consumer<StoreWriter>> taskQueue = new LinkedBlockingQueue<>();
	private Thread taskThread;
	private Consumer<StoreWriter> taskQueuePoisonPill = x -> {};
	

	public TaskQueueTransactionManager(StoreWriter storeWriter) {
		super(storeWriter);
		
		taskThread = new Thread(() -> {
			boolean stopped = false;
			while (!stopped) {
				try {
					Consumer<StoreWriter> task = taskQueue.take();
					if (task == taskQueuePoisonPill) {
						log.info("Stopping Task execution");
						stopped = true;
					} else {
						task.accept(storeWriter);
					}	
				} catch (InterruptedException e) {
					log.error("Task execution interrupted", e);
				}				
			}
			log.info("Task execution thread stopped");
		});
		
		taskThread.start();
	}

	public void executeAsync(Transaction transaction, Consumer<TransactionEvent> callback) {
		async(sw -> {
			sw.execute(transaction);
			
			if (callback != null) {
				callback.accept(transaction);
			}
		});
	}
	
	/**
	 * Blocking
	 * 
	 * @param transaction the transaction to be executed
	 * @return the transaction change description
	 */
	public TransactionEvent execute(Transaction transaction) {
		sync(sw -> {
			sw.execute(transaction);
		});
		return transaction;
	}
	
	@Override
	public void close() {
		sync(taskQueuePoisonPill);
	}

	@Override
	public void locked(Consumer<TransactionFactory> consumer) {
		sync(sw -> {
			immediate(consumer);
		});
	}

	private void async(Consumer<StoreWriter> consumer) {
		try {
			taskQueue.put(consumer);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}		
	}
	
	private void sync(Consumer<StoreWriter> consumer) {
		var countDownLatch = new CountDownLatch(1);
		GenericWrapper<Exception> exceptionWrapper = new GenericWrapper<>();  
		try {
			taskQueue.put(sw -> {
				try {
					consumer.accept(sw);;
				} catch (Exception e) {
					exceptionWrapper.setWrappedObject(e);
				} finally {
					countDownLatch.countDown();
				}
			});
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}		
		// wait until termination
		try {
			countDownLatch.await();
			
			if (exceptionWrapper.getWrappedObject() != null) {
				if (exceptionWrapper.getWrappedObject() instanceof RuntimeException) {
					throw (RuntimeException) exceptionWrapper.getWrappedObject();
				} else {
					throw new RuntimeException(exceptionWrapper.getWrappedObject());
				}	
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void lockedAsync(Consumer<TransactionFactory> consumer) {
		async(sw -> {
			immediate(consumer);
		});
	}

}