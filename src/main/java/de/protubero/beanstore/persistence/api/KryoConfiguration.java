package de.protubero.beanstore.persistence.api;

import com.esotericsoftware.kryo.kryo5.Registration;
import com.esotericsoftware.kryo.kryo5.Serializer;

public interface KryoConfiguration {

	<T> Registration register(Class<T> type, Serializer<T> serializer, int id);

	void register(Class<?> propertyBeanClass);

}
