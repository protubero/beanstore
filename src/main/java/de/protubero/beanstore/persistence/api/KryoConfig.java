package de.protubero.beanstore.persistence.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.esotericsoftware.kryo.kryo5.Serializer;

@Retention(RUNTIME)
@Target(TYPE)
public @interface KryoConfig {

	int id();
	
	@SuppressWarnings("rawtypes")
	Class<? extends Serializer> serializer();
	
}
