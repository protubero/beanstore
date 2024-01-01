package de.protubero.beanstore.persistence.kryo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.ObjectArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.ImmutableSerializer;

import de.protubero.beanstore.persistence.api.PersistentProperty;
import de.protubero.beanstore.persistence.api.SetPropertyValue;


public class KryoDefaultSerializer extends ImmutableSerializer<Object>{

	public static final Logger log = LoggerFactory.getLogger(KryoDefaultSerializer.class);
	
	// geht vermutlich eleganter :-)
	private static PersistentProperty[] DUMMY_ARRAY = new PersistentProperty[] {};		
	
	
	public class BeanInfo {
		private String alias;
		private Field[] fields;
		private boolean implementsSetPropertyValue;
		private Class<?> propertyBeanClass;

		public BeanInfo(String alias, Field[] fields, boolean anImplementsSetPropertyValue, Class<?> aPropertyBeanClass) {
			this.alias = Objects.requireNonNull(alias);
			this.fields = Objects.requireNonNull(fields);
			this.propertyBeanClass = Objects.requireNonNull(aPropertyBeanClass);
			
			implementsSetPropertyValue = anImplementsSetPropertyValue;
		}

		public Field[] getFields() {
			return fields;
		}

		public String getAlias() {
			return alias;
		}

		public boolean isImplementsSetPropertyValue() {
			return implementsSetPropertyValue;
		}

		public Class<?> getPropertyBeanClass() {
			return propertyBeanClass;
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

	private Map<Class<?>, BeanInfo> beanInfoByClassMap = new HashMap<>();
	private Map<String, BeanInfo> beanInfoByAliasMap = new HashMap<>();

	private ObjectArraySerializer arraySerializer;
	
	public KryoDefaultSerializer(Kryo kryo, Map<String, Class<?>> propertyBeanMap) {
		arraySerializer = new DefaultArraySerializers.ObjectArraySerializer(kryo, DUMMY_ARRAY.getClass());
		arraySerializer.setAcceptsNull(false);
		arraySerializer.setElementsAreSameType(true);
		arraySerializer.setElementsCanBeNull(false);
		
		List<Field> fieldList = new ArrayList<>();
		
		for (Map.Entry<String, Class<?>> entry : propertyBeanMap.entrySet()) {
			log.info("Registering property bean class " + entry.getKey()  +" (" + entry.getValue() + ")");
			
			try {
				if (entry.getValue().getConstructor() == null) {
					throw new RuntimeException("Missing no-arg constructor: " + entry.getValue());
				}
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
			
			
			boolean implementsSetPropertyValue = SetPropertyValue.class.isAssignableFrom(entry.getValue());
			Field[] fields = entry.getValue().getDeclaredFields();

		    for (Field field: fields) {
		        field.setAccessible(true);
		        fieldList.add(field);
		    }
		    
		    BeanInfo beanInfo = new BeanInfo(
		    		entry.getKey(),
		    		fieldList.toArray(new Field[fieldList.size()]),
		    		implementsSetPropertyValue,
		    		entry.getValue()
		    		);
		    
		    beanInfoByClassMap.put(entry.getValue(), beanInfo); 
		    beanInfoByAliasMap.put(entry.getKey(), beanInfo); 
		}
	}
	
	@Override
	public void write(Kryo kryo, Output output, Object object) {
		BeanInfo beanInfo = beanInfoByClassMap.get(object.getClass());
		if (beanInfo == null) {
			throw new RuntimeException("Cannot serialize class " + object.getClass());
		}
		PersistentProperty[] propertyArray = new PersistentProperty[beanInfo.getFields().length];
		for (int idx = 0; idx < beanInfo.getFields().length; idx++) {
			Field field = beanInfo.getFields()[idx];
			try {				
				Object value = field .get(object);
				propertyArray[idx] = PersistentProperty.of(field.getName(), value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("Error reading property bean value " + field.getName(), e);
			}
		}

		output.writeString(beanInfo.getAlias());
		arraySerializer.write(kryo, output, propertyArray);
	}

	@Override
	public Object read(Kryo kryo, Input input, Class<? extends Object> type) {
		String alias = input.readString();
		PersistentProperty[] propertyArray = (PersistentProperty[]) arraySerializer.read(kryo, input, DUMMY_ARRAY.getClass());
		
		BeanInfo beanInfo = beanInfoByAliasMap.get(alias);
		if (beanInfo == null) {
			throw new RuntimeException("Cannot deserialize class " + alias);
		}
		try {
			Object newInstance = beanInfo.getPropertyBeanClass().getConstructor().newInstance();
			
			for (PersistentProperty property : propertyArray) {
				if (beanInfo.isImplementsSetPropertyValue()) {
						((SetPropertyValue) newInstance).setPropertyValue(property.getProperty(), property.getValue());
				} else {
					Field field = beanInfo.fieldByName(property.getProperty());
					if (field == null) {
						throw new RuntimeException("Invalid PropertyBean property " + property.getProperty()  + " of " + beanInfo.getPropertyBeanClass());
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

	
}
