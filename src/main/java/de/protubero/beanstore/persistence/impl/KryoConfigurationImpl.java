package de.protubero.beanstore.persistence.impl;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Registration;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.InstantSerializer;

import de.protubero.beanstore.persistence.api.KryoConfiguration;
import de.protubero.beanstore.persistence.api.KryoId;
import de.protubero.beanstore.persistence.base.PersistentTransaction;

public class KryoConfigurationImpl implements KryoConfiguration {

	public static final Logger log = LoggerFactory.getLogger(KryoConfigurationImpl.class);
	
	private Kryo kryo;

	
	public KryoConfigurationImpl() {
		kryo = new Kryo();
		kryo.setRegistrationRequired(true);
		kryo.setWarnUnregisteredClasses(true);
		
		kryo.register(PersistentTransaction.class, new PersistentTransactionSerializer(),  20);
//		kryo.register(PersistentInstanceTransaction.class, 21);
//		kryo.register(PersistentInstanceTransaction[].class, 22);
//		kryo.register(PersistentProperty[].class, 23);
//		kryo.register(PersistentProperty.class, 24);
		kryo.register(Instant.class, new InstantSerializer(), 25);
//		kryo.register(PersistentBean.class, new PersistentBeanSerializer(), 26);
		
	}
	
	@Override
	public void register(Class<?> propertyBeanClass) {
		KryoId pbAnnotation = propertyBeanClass.getAnnotation(KryoId.class);
		if (pbAnnotation == null) {
			throw new RuntimeException("Property bean classes must be annotated with PropertyBean annotation");
		}
		
		int serializationId = pbAnnotation.value();
		
		log.info("Registering property bean class " + propertyBeanClass + "[" + serializationId + "]");

		kryo.register(propertyBeanClass, new PropertyBeanSerializer(kryo, propertyBeanClass), serializationId);		
	}
	
	@Override
	public <T> Registration register(Class<T> type, Serializer<T> serializer, int id) {
		if (id < 100) {
			throw new RuntimeException("IDs < 100 are reserved");
		}
		return kryo.register(type, serializer, id);
	}

	
	Kryo getKryo() {
		return kryo;
	}

	
}
