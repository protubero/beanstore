package de.protubero.beanstore.persistence.kryo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import de.protubero.beanstore.persistence.api.PersistenceException;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.persistence.api.TransactionWriter;

public class KryoPersistence implements TransactionPersistence {

	public static final Logger log = LoggerFactory.getLogger(KryoPersistence.class);
	
	
	private File file;
	private TransactionWriter writer;
	private KryoConfiguration config;
	private FileOutputStream fileOutputStream; 
	private int lastWrittenSeqNum = -1;
	
	public static KryoPersistence of(File file, KryoConfiguration config) {
		return new KryoPersistence(file, config);
	}
	
	KryoPersistence(File file, KryoConfiguration config) {
		this.file = Objects.requireNonNull(file);
		this.config = Objects.requireNonNull(config);
								
		// path must not be a directory path
		if (file.isDirectory()) {
			throw new PersistenceException("path parameter is a directory");
		}
		
		writer = new TransactionWriter() {

			boolean closed;

			@Override
			public void append(Iterator<PersistentTransaction> transactions) {
				if (closed) {
					throw new PersistenceException("writing to a closed writer");
				}
					
				// conservative implementation, subject to performance optimization
				ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
				try (Output output = new Output(out)) {
					while (transactions.hasNext()) {
						PersistentTransaction nextTransaction = transactions.next();
						
						if (lastWrittenSeqNum == -1) {
							if (1 != nextTransaction.getSeqNum()) {
								throw new AssertionError("First transaction number is unexpectedly != 1. It is " + nextTransaction.getSeqNum());
							}
							lastWrittenSeqNum = nextTransaction.getSeqNum();
						} else {
							if (lastWrittenSeqNum != (nextTransaction.getSeqNum() - 1)) {
								throw new AssertionError("Transaction number not in sequence " + lastWrittenSeqNum + " -> " + nextTransaction.getSeqNum());
							}
							lastWrittenSeqNum = nextTransaction.getSeqNum();
						}
						
						KryoPersistence.this.getKryo().writeObject(output, nextTransaction);
					}
				}
				try (FileOutputStream fileOutputStream = new FileOutputStream(file, true)) {
					
					// lock file
					FileChannel channel = fileOutputStream.getChannel();
				    FileLock lock = channel.lock();
				    
					fileOutputStream.write(out.toByteArray());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
					
			}

			@Override
			public void close() throws Exception {
				if (closed) {
					throw new PersistenceException("Re-closing closed writer");
				}
				closed = true;
				if (fileOutputStream != null) {
					log.info("Closing output stream");
					try {
						fileOutputStream.close();
					} catch (IOException e) {
						log.error("Error closing output stream", e);
					}
				}
				
			}

			@Override
			public void flush() {
				// nothing to do
			}

		};
	}

	Kryo getKryo() {
		return ((KryoConfigurationImpl) config).getKryo();
	}

	private Input input() {
		if (isEmpty()) {
			return null;
		}
		try {
			return new Input(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TransactionReader reader() {
		return new TransactionReader() {

			@Override
			public void load(Consumer<PersistentTransaction> transactionConsumer) {
				Input tInput = input();

				if (tInput == null) {
					return;
				}

				try (Input input = tInput) {
					while (true) {
						PersistentTransaction po = KryoPersistence.this.getKryo().readObject(input, PersistentTransaction.class);
						transactionConsumer.accept(po);
						lastWrittenSeqNum = po.getSeqNum();
					}
				} catch (KryoException exc) {
					if (exc.getMessage().contains("Buffer underflow")) {
						// that is the expected way how this party ends
					} else {
						throw new PersistenceException("error reading kryo data", exc);
					}
				}
			}

		};
	}

	@Override
	public boolean isEmpty() {
		return !file.exists();
	}

	@Override
	public TransactionWriter writer() {
		return writer;
	}

	public KryoConfiguration getConfig() {
		return config;
	}

	@Override
	public void lockConfiguration() {
		((KryoConfigurationImpl) config).lock();
	}



}
