package de.protubero.beanstore.impl;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreCallbacks;
import de.protubero.beanstore.api.BeanStoreMetaInfo;
import de.protubero.beanstore.api.BeanStoreState;
import de.protubero.beanstore.api.BeanStoreTransactionResult;
import de.protubero.beanstore.api.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.api.ExecutableLockedBeanStoreTransaction;
import de.protubero.beanstore.base.entity.GenericWrapper;
import de.protubero.beanstore.store.CompanionShip;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;

class BeanStoreImpl implements BeanStore {

	public static final Logger log = LoggerFactory.getLogger(BeanStoreImpl.class);

	private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
	
	private ImmutableEntityStoreSet store;
	private Runnable onCloseCallback;
	private StoreWriter storeWriter;
	
	private Thread taskThread;
		
	BeanStoreImpl(ImmutableEntityStoreSet store, Runnable onCloseCallback) {
		this.onCloseCallback = Objects.requireNonNull(onCloseCallback);
		this.store = Objects.requireNonNull(store);

		
		storeWriter = new StoreWriter();	
		
		taskThread = new Thread(() -> {
			boolean stopped = false;
			while (!stopped) {
				try {
					Runnable task = taskQueue.take();
					log.debug("Consuming next task");
					try {
						task.run();
					} catch (PoisonPillError ppe) {
						log.debug("Swallowed poison pill");
						stopped = true;
					} catch (Exception e) {
						log.error("Exception during task execution", e);
					}
				} catch (InterruptedException e) {
					log.error("Task taking interrupted", e);
				}				
			}
			log.info("Task execution thread stopped");
		});
		taskThread.setUncaughtExceptionHandler((thread, throwable) -> {
			log.error("Uncaught task execution exception", throwable);
		});
		taskThread.start();

	}
	
	private void taskAsync(Runnable task) {
		try {
			taskQueue.put(task);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void taskSync(Runnable func) {
		var countDownLatch = new CountDownLatch(1);
		GenericWrapper<Exception> exceptionWrapper = new GenericWrapper<>();  
		try {
			log.debug("Insert sync task into task execution thread");
			taskQueue.put(() -> {
				try {
					func.run();
				} catch (Exception e) {
					exceptionWrapper.setWrappedObject(e);
				} finally {
					log.debug("Sync Task has been executed ");
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
	public void close() {
		onCloseCallback.run();
	}
	
	@Override
	public ExecutableBeanStoreTransaction transaction() {
		Transaction transaction = Transaction.of(store);
		return new ExecutableBeanStoreTransactionImpl(transaction, this);
	}

	
	BeanStoreTransactionResult exec(Transaction transaction) {
		BeanStoreTransactionResult result;
		ImmutableEntityStoreSet sourceStoreSet = store;
		ImmutableEntityStoreSet resultStoreSet = storeWriter.execute(transaction, sourceStoreSet);
		result = new BeanStoreTransactionResultImpl(transaction, 
				new BeanStoreStateImpl(sourceStoreSet), new BeanStoreStateImpl(resultStoreSet));
		store = resultStoreSet;
		return result;
	}

	BeanStoreTransactionResult executeSync(Transaction transaction) {
		GenericWrapper<BeanStoreTransactionResult> resultWrapper = new GenericWrapper<>();
		taskSync(() -> {
			resultWrapper.setWrappedObject(exec(transaction));
		});
		return resultWrapper.getWrappedObject();
	}
	
	CompletableFuture<BeanStoreTransactionResult> executeAsync(Transaction transaction) {
		CompletableFuture<BeanStoreTransactionResult> result = new CompletableFuture<>();
		taskAsync(() -> {
			BeanStoreTransactionResult transactionResult = exec(transaction);
			result.complete(transactionResult);
		});
		return result;
	}
	
	@Override
	public void locked(Consumer<ExecutableLockedBeanStoreTransaction> consumer) {
		taskSync(() -> {
			// the creation of the transaction has to be within the task
			Transaction transaction = Transaction.of(store);
			ExecutableLockedBeanStoreTransaction bsTransaction = new ExecutableLockedBeanStoreTransactionImpl(transaction, this);
			consumer.accept(bsTransaction);	
		});
	}

	@Override
	public void lockedAsync(
			Consumer<ExecutableLockedBeanStoreTransaction> consumer) {
		taskAsync(() -> {
			// the creation of the transaction has to be within the task
			Transaction transaction = Transaction.of(store);
			ExecutableLockedBeanStoreTransaction bsTransaction = new ExecutableLockedBeanStoreTransactionImpl(transaction, this);
			consumer.accept(bsTransaction);	
		});
	}
	
	@Override
	public BeanStoreState state() {
		return new BeanStoreStateImpl(store);
	}

	@Override
	public BeanStoreCallbacks callbacks() {
		return new BeanStoreCallbacksImpl(storeWriter);
	}

	@Override
	public BeanStoreMetaInfo meta() {
		return new BeanStoreMetaInfoImpl(store);
	}




}
