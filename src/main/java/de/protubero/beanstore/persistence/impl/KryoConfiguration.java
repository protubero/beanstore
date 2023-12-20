package de.protubero.beanstore.persistence.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Registration;
import com.esotericsoftware.kryo.kryo5.Serializer;

import de.protubero.beanstore.persistence.api.PropertyBean;
import de.protubero.beanstore.persistence.base.PersistentBean;
import de.protubero.beanstore.persistence.base.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.base.PersistentProperty;
import de.protubero.beanstore.persistence.base.PersistentTransaction;

public class KryoConfiguration {

	public static final Logger log = LoggerFactory.getLogger(KryoConfiguration.class);
	
	private Kryo kryo;

//	private Map<Integer, Class<?>> propertyBeanMap = new HashMap<>();
	
	
	public KryoConfiguration() {
		kryo = new Kryo();
		kryo.setRegistrationRequired(true);
		kryo.setWarnUnregisteredClasses(true);
		
		kryo.register(PersistentTransaction.class, 20);
		kryo.register(PersistentInstanceTransaction.class, 21);
		kryo.register(PersistentInstanceTransaction[].class, 22);
		kryo.register(PersistentProperty[].class, 23);
		kryo.register(PersistentProperty.class, 24);
		kryo.register(Instant.class, 25);
		kryo.register(PersistentBean.class, 26);
		
	}
	
	public void registerProperyBean(Class<?> propertyBeanClass) {
		PropertyBean pbAnnotation = propertyBeanClass.getAnnotation(PropertyBean.class);
		if (pbAnnotation == null) {
			throw new RuntimeException("Property bean classes must be annotated with PropertyBean annotation");
		}
		
		int serializationId = pbAnnotation.value();
		
		log.info("Registering property bean class " + propertyBeanClass + "[" + serializationId + "]");

		kryo.register(propertyBeanClass, new PropertyBeanSerializer(kryo, propertyBeanClass), serializationId);		
		
//		if (propertyBeanMap.containsKey(serializationId)) {
//			throw new RuntimeException("Duplicate property bean with serialization id: " + serializationId);
//		}
//		propertyBeanMap.put(serializationId, propertyBeanClass);
	}
	
	public <T> Registration register(Class<T> type, Serializer<T> serializer, int id) {
		if (id < 100) {
			throw new RuntimeException("IDs < 100 are reserved");
		}
		return kryo.register(type, serializer, id);
	}

	
	Kryo getKryo() {
		return kryo;
	}

//	public Map<Integer, Class<?>> getPropertyBeanMap() {
//		return propertyBeanMap;
//	}
	
	
	
}
