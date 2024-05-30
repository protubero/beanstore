package de.protubero.beanstore.links;

import java.util.Objects;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import de.protubero.beanstore.entity.PersistentObjectKey;
import de.protubero.beanstore.entity.PersistentObjectKeyImpl;
import de.protubero.beanstore.persistence.kryo.KryoDictionary;

public class PersistentObjectKeyKryoSerializer extends Serializer<PersistentObjectKeyImpl<?>> {

	private KryoDictionary dictionary;

	public PersistentObjectKeyKryoSerializer(KryoDictionary aDictionary) {
		dictionary = Objects.requireNonNull(aDictionary);
	}

	@Override
	public void write(Kryo kryo, Output output, PersistentObjectKeyImpl<?> key) {
		output.writeInt(dictionary.getOrCreate(key.alias()), true);
		output.writeLong(key.id(), true);
	}

	@Override
	public PersistentObjectKeyImpl<?> read(Kryo kryo, Input input, Class<? extends PersistentObjectKeyImpl<?>> type) {
		String alias = dictionary.textByCode(input.readInt(true));
		long id = input.readLong(true);
		return (PersistentObjectKeyImpl<?>) PersistentObjectKey.of(alias, id);
	}

}
