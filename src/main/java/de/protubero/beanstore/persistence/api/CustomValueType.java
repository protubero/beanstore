package de.protubero.beanstore.persistence.api;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers;

public interface CustomValueType {

	public static DefaultArraySerializers.ByteArraySerializer BYTE_ARRAY_SERIALIZER = new DefaultArraySerializers.ByteArraySerializer();
	
	
	default byte[] bytes() {
		throw new RuntimeException("bytes() not implemented");
	}

	default void bytes(byte[] aByteArray) {
		throw new RuntimeException("bytes(byte[]) not implemented");
	}
	
	
	default void write(Kryo kryo, Output output) {
		BYTE_ARRAY_SERIALIZER.write(kryo, output, bytes());
	}

	default void read(Kryo kryo, Input input) {
		byte[] bytes = BYTE_ARRAY_SERIALIZER.read(kryo, input, byte[].class);
		bytes(bytes);
	}

	
}
