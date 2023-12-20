package de.protubero.beanstore.persistence.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.ObjectArraySerializer;

import de.protubero.beanstore.persistence.api.SetPropertyValue;
import de.protubero.beanstore.persistence.base.PersistentProperty;

public class PropertyBeanSerializer extends Serializer {

	public static final Logger log = LoggerFactory.getLogger(PropertyBeanSerializer.class);
	
	
	// geht vermutlich eleganter :-)
	private static PersistentProperty[] DUMMY_ARRAY = new PersistentProperty[] {};		
	
	
	private Class<?> beanClass;
	private Field[] fields;
	private boolean implementsSetPropertyValue;

	private ObjectArraySerializer arraySerializer;
	
	
	public PropertyBeanSerializer(Kryo kryo, Class<?> aBeanClass) {
		beanClass = Objects.requireNonNull(aBeanClass);
		
		arraySerializer = new DefaultArraySerializers.ObjectArraySerializer(kryo, DUMMY_ARRAY.getClass());
		arraySerializer.setAcceptsNull(false);
		arraySerializer.setElementsAreSameType(true);
		arraySerializer.setElementsCanBeNull(false);
		
			
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
		PersistentProperty[] propertyArray = new PersistentProperty[fields.length];
		for (int idx = 0; idx < fields.length; idx++) {
			Field field = fields[idx];
			try {				
				Object value = field.get(object);
				propertyArray[idx] = PersistentProperty.of(field.getName(), value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("Error reading property bean value " + field.getName(), e);
			}
		}

		arraySerializer.write(kryo, output, propertyArray);
	}

	@Override
	public Object read(Kryo kryo, Input input, Class type) {
		PersistentProperty[] propertyArray = (PersistentProperty[]) arraySerializer.read(kryo, input, DUMMY_ARRAY.getClass());
		
		try {
			Object newInstance = beanClass.getConstructor().newInstance();
			
			for (PersistentProperty property : propertyArray) {
				if (implementsSetPropertyValue) {
						((SetPropertyValue) newInstance).setPropertyValue(property.getProperty(), property.getValue());
				} else {
					Field field = fieldByName(property.getProperty());
					if (field == null) {
						throw new RuntimeException("Invalid PropertyBean property " + property.getProperty()  + " of " + beanClass);
					}
					field.set(newInstance, property.getValue());
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
