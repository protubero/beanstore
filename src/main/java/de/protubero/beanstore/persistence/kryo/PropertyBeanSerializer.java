package de.protubero.beanstore.persistence.kryo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import de.protubero.beanstore.persistence.api.AfterDeserialization;
import de.protubero.beanstore.persistence.api.SetPropertyValue;
import de.protubero.beanstore.persistence.api.SetPropertyValueContext;

@SuppressWarnings("rawtypes")
public class PropertyBeanSerializer extends Serializer implements DictionaryUsing {

	public static final Logger log = LoggerFactory.getLogger(PropertyBeanSerializer.class);
	
	private Class<?> beanClass;
	private Field[] fields;
	
	private boolean implementsSetPropertyValue;
	private boolean implementsAfterDeserialization;

	private Map<String, Field> fieldNameMap = new HashMap<>();
	
	private KryoDictionary dictionary;
	
	
	public PropertyBeanSerializer(Class<?> aBeanClass) {
		beanClass = Objects.requireNonNull(aBeanClass);
			
		try {
			if (aBeanClass.getConstructor() == null) {
				throw new RuntimeException("Missing no-arg constructor: " + aBeanClass);
			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
			
		implementsSetPropertyValue = SetPropertyValue.class.isAssignableFrom(aBeanClass);
		implementsAfterDeserialization = AfterDeserialization.class.isAssignableFrom(aBeanClass);
		
		Field[] fieldList = aBeanClass.getDeclaredFields();

		List<Field> resultFieldList = new ArrayList<>();
		
	    for (Field field: fieldList) {
			boolean isTransient = Modifier.isTransient(field.getModifiers());
			if (!isTransient) {
		        field.setAccessible(true);
		        resultFieldList.add(field);

		        fieldNameMap.put(field.getName(), field);
		        KryoAlias aliasAnnotation = field.getAnnotation(KryoAlias.class);
				if (aliasAnnotation != null) {
					for (String fieldAlias : Objects.requireNonNull(aliasAnnotation.value())) {
						fieldNameMap.put(Objects.requireNonNull(fieldAlias), field);
					}
				}
			}    
	    }
	    
	    fields = resultFieldList.toArray(new Field[resultFieldList.size()]);
	}
	

	@Override
	public void write(Kryo kryo, Output output, Object object) {
		output.writeVarInt(fields.length, true);
		
		for (int idx = 0; idx < fields.length; idx++) {
			Field field = fields[idx];
			try {				
				Object value = field.get(object);

				output.writeInt(dictionary.getOrCreate(field.getName()), true);
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
				String key = dictionary.textByCode(input.readInt(true));
				Object value = kryo.readClassAndObject(input);
				
				if (implementsSetPropertyValue) {
					((SetPropertyValue) newInstance).setPropertyValue(key, value, new SetPropertyValueContext() {
						
						@Override
						public void setFieldValue() {
							PropertyBeanSerializer.this.setFieldValue(newInstance, key, value);
						}
					});
				} else {
					setFieldValue(newInstance, key, value);
				}
			}	

			
			if (implementsAfterDeserialization) {
				((AfterDeserialization) newInstance).afterDeserialization();
			}
			
			return newInstance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}


	private void setFieldValue(Object newInstance, String key, Object value)  {
		Field field = fieldNameMap.get(key);
		if (field == null) {
			throw new RuntimeException("Invalid PropertyBean property " + key  + " of " + beanClass);
		}
		try {
			field.set(newInstance, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
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

	@Override
	public void setDictionary(KryoDictionary dictionary) {
		this.dictionary = dictionary;
	}
	

}
