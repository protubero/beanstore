package de.protubero.beanstore.collections;

import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

public class MapPSetKryoSerializer extends Serializer<MapPSet<?>> {

	@Override
	public void write(Kryo kryo, Output output, MapPSet<?> aSet) {
		output.writeVarInt(aSet.size(), true);
		for (var elt : aSet) {
			kryo.writeClassAndObject(output, elt);
		}
	}

	@Override
	public MapPSet<?> read(Kryo kryo, Input input, Class<? extends MapPSet<?>> type) {
		MapPSet<Object> result = HashTreePSet.empty();
		int count = input.readVarInt(true);
		for (int i = 0; i < count; i++) {
			Object obj = kryo.readClassAndObject(input);
			result = result.plus(obj);
		}
		return result;
	}



}
