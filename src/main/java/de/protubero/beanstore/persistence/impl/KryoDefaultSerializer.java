package de.protubero.beanstore.persistence.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers;
import com.esotericsoftware.kryo.kryo5.serializers.ImmutableSerializer;

import de.protubero.beanstore.persistence.api.CustomValueType;

public class KryoDefaultSerializer extends ImmutableSerializer<CustomValueType> {

	private static DefaultArraySerializers.ByteArraySerializer BYTE_ARRAY_SERIALIZER = new DefaultArraySerializers.ByteArraySerializer();
	
	{
		setAcceptsNull(true);
	}
	
	public void write (Kryo kryo, Output output, CustomValueType cvt) {
		BYTE_ARRAY_SERIALIZER.write(kryo, output, cvt.asBytes());
	}

	@SuppressWarnings("unchecked")
	public CustomValueType read (Kryo kryo, Input input, Class type) {
		byte[] bytes = null;

		bytes = BYTE_ARRAY_SERIALIZER.read(kryo, input, byte[].class);
		try {
			Constructor<CustomValueType> constructor = type.getConstructor(byte[].class);
			try {
				return constructor.newInstance(bytes);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException("Error instantiating custom value type " + type, e);
			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Missing deserialization constructor", e);
		}
	}


}
