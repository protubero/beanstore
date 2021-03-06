package de.protubero.beanstore.persistence.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.Instant;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import de.protubero.beanstore.persistence.api.PersistenceException;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.persistence.api.TransactionWriter;
import de.protubero.beanstore.persistence.base.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.base.PersistentPropertyUpdate;
import de.protubero.beanstore.persistence.base.PersistentTransaction;

public class KryoPersistence implements TransactionPersistence {

	private Kryo kryo;
	private TransactionWriter writer;
	private File file;

	
	public KryoPersistence(File file) {
		this.file = Objects.requireNonNull(file);
		
		// path must not be a directory path
		if (file.isDirectory()) {
			throw new PersistenceException("path parameter is a directory");
		}
		

		kryo = new Kryo();
		
		kryo.register(PersistentTransaction.class, 20);
		kryo.register(PersistentInstanceTransaction.class, 21);
		kryo.register(PersistentInstanceTransaction[].class, 22);
		kryo.register(PersistentPropertyUpdate[].class, 23);
		kryo.register(PersistentPropertyUpdate.class, 24);
		kryo.register(Instant.class, 25);

		
		writer = new TransactionWriter() {

			boolean closed;

			@Override
			public void append(Iterator<PersistentTransaction> transactions) {
				if (closed) {
					throw new PersistenceException("writing to a closed writer");
				}
				try (Output output = output()) {
					while (transactions.hasNext()) {
						kryo.writeObject(output, transactions.next());
					}
				}
			}

			@Override
			public void close() throws Exception {
				closed = true;
			}

			@Override
			public void flush() {
				// nothing to do
			}

		};
	}

	private Output output() {
		try {
			return new Output(new FileOutputStream(file, true));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
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

	public Kryo getKryo() {
		return kryo;
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
						PersistentTransaction po = kryo.readObject(input, PersistentTransaction.class);
						transactionConsumer.accept(po);
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
	public TransactionWriter writer() {
		return writer;
	}

	@Override
	public boolean isEmpty() {
		return !file.exists();
	}


}
