package de.protubero.beanstore.persistence.impl;

import java.lang.reflect.InvocationTargetException;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.ImmutableSerializer;

import de.protubero.beanstore.persistence.api.CustomValueType;

public class KryoDefaultSerializer extends ImmutableSerializer<CustomValueType> {

	
	{
		setAcceptsNull(true);
	}
	
	public void write (Kryo kryo, Output output, CustomValueType cvt) {
		cvt.write(kryo, output);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CustomValueType read (Kryo kryo, Input input, Class type) {
		CustomValueType cvt;
		try {
			cvt = (CustomValueType) type.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Error initialising custom value of type " + type, e);
		}
		cvt.read(kryo, input);
		return cvt;
	}


}
