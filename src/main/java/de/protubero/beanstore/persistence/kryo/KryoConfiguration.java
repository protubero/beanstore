package de.protubero.beanstore.persistence.kryo;

import com.esotericsoftware.kryo.kryo5.Registration;
import com.esotericsoftware.kryo.kryo5.Serializer;

public interface KryoConfiguration {

	public static KryoConfiguration create() {
		return new KryoConfigurationImpl();
	}
	
	<T> Registration register(Class<T> type, Serializer<T> serializer, int id);

	<T> Registration register(Class<T> type, Class<? extends Serializer> serializerClass, int id);


}
