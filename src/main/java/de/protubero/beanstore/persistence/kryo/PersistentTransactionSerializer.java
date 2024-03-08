package de.protubero.beanstore.persistence.kryo;

import java.time.Instant;
import java.util.Objects;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.InstantSerializer;

import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.api.PersistentProperty;
import de.protubero.beanstore.persistence.api.PersistentTransaction;

public class PersistentTransactionSerializer extends Serializer<PersistentTransaction> {

	private InstantSerializer instantSerializer = new InstantSerializer(); 
	private KryoDictionary dictionary;
	
	public PersistentTransactionSerializer(KryoDictionary dictionary) {
		this.dictionary = Objects.requireNonNull(dictionary);
	}

	@Override
	public void write(Kryo kryo, Output output, PersistentTransaction pt) {
		if (dictionary.hasNewEntries()) {
			throw new AssertionError();
		}
		
		// mark transaction entry
		output.writeByte(0);

		// serialization version
		output.writeByte(0);
		
		// write dict entries
		

		output.writeInt(pt.getSeqNum(), true);
		output.writeString(pt.getDescription());

		if (pt.getTimestamp() == null) {
			instantSerializer.write(kryo, output, Instant.now());
		} else {
			instantSerializer.write(kryo, output, pt.getTimestamp());
		}	
		output.writeByte(pt.getTransactionType());
		kryo.writeObjectOrNull(output, pt.getMigrationId(), String.class);
		if (pt.getInstanceTransactions() == null) {
			output.writeVarInt(0, true);
		} else {
			output.writeVarInt(pt.getInstanceTransactions().length, true);
			
			for (PersistentInstanceTransaction pit : pt.getInstanceTransactions()) {
				output.writeByte(pit.getType());
				output.writeVarLong(pit.getId(), true);
				output.writeVarInt(pit.getVersion(), true);
				Integer aliasCode = dictionary.getOrCreate(Objects.requireNonNull(pit.getAlias()));
				output.writeInt(aliasCode, true);
				
				if (pit.getPropertyUpdates() == null) {
					output.writeVarInt(0, true);
				} else {
					output.writeVarInt(pit.getPropertyUpdates().length, true);
					for (PersistentProperty prop : pit.getPropertyUpdates()) {
						Integer code = dictionary.getOrCreate(Objects.requireNonNull(prop.getProperty()));
						output.writeInt(code.intValue(), true);
						kryo.writeClassAndObject(output, prop.getValue());
					}
				}
			}
		}
		
	}

	@Override
	public PersistentTransaction read(Kryo kryo, Input input, Class<? extends PersistentTransaction> type) {
		byte entryType = input.readByte();
		if (entryType != 0) {
			throw new AssertionError();
		}

		byte serializationVersion = input.readByte();
		if (serializationVersion != 0) {
			throw new AssertionError();
		}
		
		PersistentTransaction pt = new PersistentTransaction();
		
		pt.setSeqNum(input.readVarInt(true));
		pt.setDescription(input.readString());
		pt.setTimestamp(instantSerializer.read(kryo, input, Instant.class));
		pt.setTransactionType(input.readByte());
		pt.setMigrationId(kryo.readObjectOrNull(input, String.class));
		
		int numInstanceTransactions = input.readVarInt(true);
		if (numInstanceTransactions > 0) {
			PersistentInstanceTransaction[] pitArray = new PersistentInstanceTransaction[numInstanceTransactions];
			pt.setInstanceTransactions(pitArray);
			for (int i = 0; i < numInstanceTransactions; i++) {
				PersistentInstanceTransaction pit = new PersistentInstanceTransaction();
				pitArray[i] = pit;
				pit.setType(input.readByte());
				pit.setId(input.readVarLong(true));
				pit.setVersion(input.readVarInt(true));
				pit.setAlias(dictionary.textByCode(input.readInt(true)));
				
				int numProperties = input.readVarInt(true);
				if (numProperties > 0) {
					PersistentProperty[] propArray = new PersistentProperty[numProperties];
					pit.setPropertyUpdates(propArray);
					for (int j = 0; j < numProperties; j++) {
						PersistentProperty property = new PersistentProperty();
						propArray[j] = property;
						String propText = dictionary.textByCode(input.readInt(true));
						if (propText == null) {
							throw new AssertionError("Property text not found");
						}
						property.setProperty(propText);
						property.setValue(kryo.readClassAndObject(input));
					}
				}	
			}
		}	
		
		return pt;
	}

	

}
