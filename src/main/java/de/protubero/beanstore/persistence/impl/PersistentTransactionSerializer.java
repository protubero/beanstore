package de.protubero.beanstore.persistence.impl;

import java.time.Instant;
import java.util.Objects;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.InstantSerializer;

import de.protubero.beanstore.persistence.base.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.base.PersistentProperty;
import de.protubero.beanstore.persistence.base.PersistentTransaction;

public class PersistentTransactionSerializer extends Serializer<PersistentTransaction> {

	private InstantSerializer instantSerializer = new InstantSerializer(); 
	private KryoDictionary dictionary;
	
	@Override
	public void write(Kryo kryo, Output output, PersistentTransaction pt) {
		// mark transaction entry
		output.writeByte(0);

		// serialization version
		output.writeByte(0);
		
		// write dict entries
		
		
		if (pt.getTimestamp() == null) {
			instantSerializer.write(kryo, output, Instant.now());
		} else {
			instantSerializer.write(kryo, output, pt.getTimestamp());
		}	
		output.writeByte(pt.getTransactionType());
		kryo.writeObjectOrNull(output, pt.getTransactionId(), String.class);
		if (pt.getInstanceTransactions() == null) {
			output.writeVarInt(0, true);
		} else {
			output.writeVarInt(pt.getInstanceTransactions().length, true);
			
			for (PersistentInstanceTransaction pit : pt.getInstanceTransactions()) {
				output.writeByte(pit.getType());
				output.writeVarLong(pit.getId(), true);
				output.writeVarInt(pit.getVersion(), true);
				output.writeString(Objects.requireNonNull(pit.getAlias()));
				
				if (pit.getPropertyUpdates() == null) {
					output.writeVarInt(0, true);
				} else {
					output.writeVarInt(pit.getPropertyUpdates().length, true);
					for (PersistentProperty prop : pit.getPropertyUpdates()) {
						output.writeString(Objects.requireNonNull(prop.getProperty()));
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
		
		pt.setTimestamp(instantSerializer.read(kryo, input, Instant.class));
		pt.setTransactionType(input.readByte());
		pt.setTransactionId(kryo.readObjectOrNull(input, String.class));
		
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
				pit.setAlias(input.readString());
				
				int numProperties = input.readVarInt(true);
				if (numProperties > 0) {
					PersistentProperty[] propArray = new PersistentProperty[numProperties];
					pit.setPropertyUpdates(propArray);
					for (int j = 0; j < numProperties; j++) {
						PersistentProperty property = new PersistentProperty();
						propArray[j] = property;
						property.setProperty(input.readString());
						property.setValue(kryo.readClassAndObject(input));
					}
				}	
			}
		}	
		
		return pt;
	}

	

}
