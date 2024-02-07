package de.protubero.beanstore.persistence.kryo;

import java.io.ByteArrayInputStream;
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
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import de.protubero.beanstore.persistence.api.PersistenceException;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.persistence.api.PersistentTransactionConsumer;
import de.protubero.beanstore.persistence.api.TransactionPersistence;
import de.protubero.beanstore.persistence.api.TransactionReader;
import de.protubero.beanstore.persistence.api.TransactionWriter;

public class KryoPersistence implements TransactionPersistence {

	public static final Logger log = LoggerFactory.getLogger(KryoPersistence.class);


	protected static final byte CHUNK_TYPE_TRANSACTIONS = 0;
	protected static final byte CHUNK_TYPE_DICTIONARY = 1;
	
	
	private File file;
	private TransactionWriter writer;
	private KryoConfiguration config;
	private KryoDictionary dictionary;
	private Integer lastWrittenSeqNum;
	private TransactionReader reader;
	private boolean used;
	
	
	public static KryoPersistence of(File file, KryoConfiguration config) {
		return new KryoPersistence(file, config);
	}
	
	KryoPersistence(File file, KryoConfiguration config) {
		this.file = Objects.requireNonNull(file);
		this.config = Objects.requireNonNull(config);
				
		this.dictionary = ((KryoConfigurationImpl) config).getDictionary();
		
		// path must not be a directory path
		if (file.isDirectory()) {
			throw new PersistenceException("path parameter is a directory");
		}
		
		reader = new TransactionReader() {

			@Override
			public void load(PersistentTransactionConsumer transactionConsumer) {
				if (!file.exists()) {
					return;
				}

				try (Input input = new Input(new FileInputStream(file))) {
					while (transactionConsumer.wantsNextTransaction()) {
						byte chunkType = input.readByte();
						byte version = input.readByte();
						
						int elementCount = input.readInt(true);

						int byteArrayLen = input.readInt(true);
						long crc23Orig = input.readLong();
						byte[] data = input.readBytes(byteArrayLen);
						
						Checksum crc32 = new CRC32();
						crc32.update(data, 0, data.length);
						if (crc32.getValue() != crc23Orig) {
							throw new RuntimeException("CRC32 error reading file " + file);
						}

						try (Input nestedInput = new Input(new ByteArrayInputStream(data))) {
							switch (chunkType) {
							case CHUNK_TYPE_TRANSACTIONS:
								for (int i = 0; i < elementCount; i++) {
									PersistentTransaction po = KryoPersistence.this.getKryo().readObject(nestedInput, PersistentTransaction.class);
									transactionConsumer.accept(po);
									lastWrittenSeqNum = po.getSeqNum();
								}
								break;
							case CHUNK_TYPE_DICTIONARY:
								dictionary.load(nestedInput);
								break;
							default:
								throw new AssertionError("Invalid chunk type " + chunkType);
								
							}
						} catch (KryoException exc) {
							throw new PersistenceException("error reading kryo data", exc);
						}	
					}
				} catch (KryoException exc) {
					if (exc.getMessage().contains("Buffer underflow")) {
						// that is the expected way how this party ends
					} else {
						throw new PersistenceException("error reading kryo data", exc);
					}
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}

		};
		
		
		writer = new TransactionWriter() {

			boolean closed;

			@Override
			public void append(Iterator<PersistentTransaction> transactions) {
				if (closed) {
					throw new PersistenceException("writing to a closed writer");
				}

				int transactionCount = 0;
				
				// conservative implementation, subject to performance optimization
				ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
				try (Output output = new Output(out)) {
					while (transactions.hasNext()) {
						transactionCount++;
						PersistentTransaction nextTransaction = transactions.next();
						
						if (lastWrittenSeqNum == null) {
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
						
						if (nextTransaction.getTransactionType() == PersistentTransaction.TRANSACTION_TYPE_MIGRATION) {
							if (nextTransaction.getTransactionId() == null) {
								throw new AssertionError();
							}
						}
						
						KryoPersistence.this.getKryo().writeObject(output, nextTransaction);
					}
				}
				byte[] byteArray = out.toByteArray();
				
				try (FileOutputStream fileOutputStream = new FileOutputStream(file, true)) {
					Output output = new Output(fileOutputStream);
					
					// lock file
					FileChannel channel = fileOutputStream.getChannel();
				    FileLock lock = channel.lock();
					
					if (dictionary.hasNewEntries()) {
						int newEntriesCount = dictionary.newEntriesCount();
						
						ByteArrayOutputStream dictByteArrayOut = new ByteArrayOutputStream(1024);
						try (Output dictOut = new Output(dictByteArrayOut)) {
							dictionary.writeNewEntries(dictOut);
						}
						byte[] dictByteArray = dictByteArrayOut.toByteArray();
						
						Checksum dictCrc32 = new CRC32();
						dictCrc32.update(dictByteArray, 0, dictByteArray.length);
												
						// chunk type
						output.writeByte(CHUNK_TYPE_DICTIONARY);
						
						// version
						output.writeByte(0);

						// element count (optional)
						output.writeInt(newEntriesCount, true);
						
						// data len
						output.writeInt(dictByteArray.length, true);
						
						// crc32 checksum
						output.writeLong(dictCrc32.getValue());
						
						// data
						output.write(dictByteArray);
					}
					
					Checksum crc32 = new CRC32();
					crc32.update(byteArray, 0, byteArray.length);
					
					
					// chunk type
					output.writeByte(CHUNK_TYPE_TRANSACTIONS);
					
					// version
					output.writeByte(0);

					// element count (optional)
					output.writeInt(transactionCount, true);
					
					
					// data len
					output.writeInt(byteArray.length, true);
					
					// crc32 checksum
					output.writeLong(crc32.getValue());
					
					// data
					output.write(byteArray);
					output.flush();
					output.close();
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
			}

			@Override
			public void flush() {
				// nothing to do
			}

		};
	}

	public KryoPersistence clonePersistence() {
		return KryoPersistence.of(file, config);
	}
	
	Kryo getKryo() {
		return ((KryoConfigurationImpl) config).getKryo();
	}


	@Override
	public TransactionReader reader() {
		return reader;
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
	public void onStartStoreBuild() {
		if (used) {
			throw new RuntimeException("Persistence is already in use by another store builder");
		}

		used = true;
		((KryoConfigurationImpl) config).lock();
	}

	@Override
	public Integer lastSeqNum() {
		return lastWrittenSeqNum;
	}



}
