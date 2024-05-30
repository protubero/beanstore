package de.protubero.beanstore.api;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.tx.TransactionFailure;

/**
 *  
 * The main BeanStore class.
 * 
 *
 */
public interface BeanStore extends BeanStoreBase {
	
	
	/**
	 * Access Store and execute transactions on a locked store.
	 * The method blocks the calling thread until the operation is finished.   
	 * 
	 */
	default void locked(Consumer<BeanStoreBase> consumer) throws TransactionFailure {
		try {
			lockedAsync(consumer).get();
		} catch (InterruptedException | ExecutionException e) {
			if (e.getCause() instanceof TransactionFailure) {
				throw (TransactionFailure) e.getCause(); 
			} else {
				throw new RuntimeException(e);
			}	
		}
	}

	<T extends AbstractPersistentObject> T get(PersistentObjectKey<T> key);

	default <T extends AbstractPersistentObject> T get(T obj) {
		return get(PersistentObjectKey.of(Objects.requireNonNull(obj)));
	}
	
	
	/**
	 * Access Store and execute transactions on a locked store.
	 * The method does NOT block the calling thread.   
	 */
	CompletableFuture<Void> lockedAsync(Consumer<BeanStoreBase> consumer);
	
	/**
	 * Returns an interface which provides access to the store callbacks.
	 * 
	 * @return a BeanStoreCallbacks instance
	 */
	BeanStoreCallbacks callbacks();

	/**
	 * Close the BeanStore.  
	 * 
	 * <p>
	 * First the transaction queue is closed and will not accept new entries.
	 * When the remaining transactions were all executed, the persistent transaction writer 
	 * is flushed and closed as well.
	 * </p>
	 * 
	 * Closing the store prevents further changes but the store is still readable.
	 * 
	 */
	void close();
	
	
}
