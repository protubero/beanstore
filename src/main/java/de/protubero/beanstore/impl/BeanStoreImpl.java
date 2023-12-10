package de.protubero.beanstore.impl;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.api.BeanStore;
import de.protubero.beanstore.api.BeanStoreCallbacks;
import de.protubero.beanstore.api.BeanStoreMetaInfo;
import de.protubero.beanstore.api.BeanStoreState;
import de.protubero.beanstore.api.BeanStoreTransactionResult;
import de.protubero.beanstore.api.ExecutableBeanStoreTransaction;
import de.protubero.beanstore.api.ExecutableLockedBeanStoreTransaction;
import de.protubero.beanstore.store.ImmutableEntityStoreSet;
import de.protubero.beanstore.writer.StoreWriter;
import de.protubero.beanstore.writer.Transaction;

class BeanStoreImpl implements BeanStore {

	public static final Logger log = LoggerFactory.getLogger(BeanStoreImpl.class);

	private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
	
	private ImmutableEntityStoreSet store;
	private StoreWriter storeWriter;
	
	private Thread taskThread;
	private boolean closed;
	
	private CompletableFuture<BeanStore> closedStoreFuture;
		
	BeanStoreImpl(ImmutableEntityStoreSet store, Runnable onCloseCallback, StoreWriter aStoreWriter) {
		this.store = Objects.requireNonNull(store);
		this.storeWriter = aStoreWriter;		
		
		taskThread = new Thread(() -> {
			boolean stopped = false;
			while (!stopped) {
				try {
					Runnable task = taskQueue.take();
					log.debug("Consuming next task");
					try {
						task.run();
					} catch (PoisonPillError ppe) {
						log.info("All tasks executed");
						stopped = true;
					} catch (Exception e) {
						log.error("Exception during task execution", e);
					}
				} catch (InterruptedException e) {
					log.error("Task taking interrupted", e);
				}				
			}
			log.info("Task execution thread stopped");
			onCloseCallback.run();
			
			if (closedStoreFuture != null) {
				closedStoreFuture.complete(this);
			}	
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
	
	/*
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
	*/
	
	@Override
	public CompletableFuture<BeanStore> close() {
		if (closed) {
			throw new RuntimeException("already closed");
		} else {
			closed = true;
			closedStoreFuture = new CompletableFuture<>();
			taskAsync(() -> {
				throw new PoisonPillError();
			});
			return closedStoreFuture;
		}	
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

	/*
	BeanStoreTransactionResult executeSync(Transaction transaction) {
		GenericWrapper<BeanStoreTransactionResult> resultWrapper = new GenericWrapper<>();
		taskSync(() -> {
			resultWrapper.setWrappedObject(exec(transaction));
		});
		return resultWrapper.getWrappedObject();
	}
	*/
	
	CompletableFuture<BeanStoreTransactionResult> execute(Transaction transaction) {
		if (closed) {
			throw new RuntimeException("Closed store does not accept transactions");
		}
		
		CompletableFuture<BeanStoreTransactionResult> result = new CompletableFuture<>();
		taskAsync(() -> {
			try {
				BeanStoreTransactionResult transactionResult = exec(transaction);
				result.complete(transactionResult);
			} catch (Exception e) {
				result.completeExceptionally(e);
			}
		});
		return result;
	}
	
	@Override
	public void locked(Consumer<Supplier<ExecutableLockedBeanStoreTransaction>> consumer) {
		if (closed) {
			throw new RuntimeException("Closed store does not accept transactions");
		}
		taskAsync(() -> {
			consumer.accept(new Supplier<>() {

				@Override
				public ExecutableLockedBeanStoreTransaction get() {
					// the creation of the transaction has to be within the task
					Transaction transaction = Transaction.of(store);
					ExecutableLockedBeanStoreTransaction bsTransaction = new ExecutableLockedBeanStoreTransactionImpl(transaction, BeanStoreImpl.this);
					return bsTransaction;
				}
				
			});	
		});
	}
/*
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
*/	
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
