package de.protubero.beanstore.persistence.kryo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import de.protubero.beanstore.persistence.api.SetPropertyValue;

@SuppressWarnings("rawtypes")
public class PropertyBeanSerializer extends Serializer {

	public static final Logger log = LoggerFactory.getLogger(PropertyBeanSerializer.class);
	
	private Class<?> beanClass;
	private Field[] fields;
	private boolean implementsSetPropertyValue;
	
	
	public PropertyBeanSerializer(Kryo kryo, Class<?> aBeanClass) {
		beanClass = Objects.requireNonNull(aBeanClass);
			
		try {
			if (aBeanClass.getConstructor() == null) {
				throw new RuntimeException("Missing no-arg constructor: " + aBeanClass);
			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
			
		implementsSetPropertyValue = SetPropertyValue.class.isAssignableFrom(aBeanClass);
		fields = aBeanClass.getDeclaredFields();

	    for (Field field: fields) {
	        field.setAccessible(true);
	    }
	}

	@Override
	public void write(Kryo kryo, Output output, Object object) {
		output.writeVarInt(fields.length, true);
		
		for (int idx = 0; idx < fields.length; idx++) {
			Field field = fields[idx];
			try {				
				Object value = field.get(object);

				output.writeString(field.getName());
				kryo.writeClassAndObject(output, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("Error reading property bean value " + field.getName(), e);
			}
		}
	}

	@Override
	public Object read(Kryo kryo, Input input, Class type) {
		int numFields = input.readVarInt(true);
		
		try {
			Object newInstance = beanClass.getConstructor().newInstance();
			
			for (int i = 0; i < numFields; i++) {
				String key = input.readString();
				Object value = kryo.readClassAndObject(input);
				
				if (implementsSetPropertyValue) {
						((SetPropertyValue) newInstance).setPropertyValue(key, value);
				} else {
					Field field = fieldByName(key);
					if (field == null) {
						throw new RuntimeException("Invalid PropertyBean property " + key  + " of " + beanClass);
					}
					field.set(newInstance, value);
				}
			}	

			return newInstance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	
	public Field fieldByName(String property) {
		for (Field field : fields) {
			if (field.getName().equals(property)) {
				return field;
			}
		}
		return null;
	}
	

}
