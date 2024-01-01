package de.protubero.beanstore.persistence.kryo;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Registration;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.DurationSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.InstantSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.LocalDateTimeSerializer;

import de.protubero.beanstore.persistence.api.KryoConfiguration;
import de.protubero.beanstore.persistence.api.PersistentTransaction;

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
		
		kryo.register(Duration.class, new DurationSerializer(), 26);
		kryo.register(LocalDateTime.class, new LocalDateTimeSerializer(), 27);
//		if (isClassAvailable("java.time.LocalDate")) kryo.addDefaultSerializer(LocalDate.class, LocalDateSerializer.class);
//		if (isClassAvailable("java.time.LocalTime")) kryo.addDefaultSerializer(LocalTime.class, LocalTimeSerializer.class);
//		if (isClassAvailable("java.time.LocalDateTime"))
//			kryo.addDefaultSerializer(LocalDateTime.class, LocalDateTimeSerializer.class);
//		if (isClassAvailable("java.time.ZoneOffset")) kryo.addDefaultSerializer(ZoneOffset.class, ZoneOffsetSerializer.class);
//		if (isClassAvailable("java.time.ZoneId")) kryo.addDefaultSerializer(ZoneId.class, ZoneIdSerializer.class);
//		if (isClassAvailable("java.time.OffsetTime")) kryo.addDefaultSerializer(OffsetTime.class, OffsetTimeSerializer.class);
//		if (isClassAvailable("java.time.OffsetDateTime"))
//			kryo.addDefaultSerializer(OffsetDateTime.class, OffsetDateTimeSerializer.class);
//		if (isClassAvailable("java.time.ZonedDateTime"))
//			kryo.addDefaultSerializer(ZonedDateTime.class, ZonedDateTimeSerializer.class);
//		if (isClassAvailable("java.time.Year")) kryo.addDefaultSerializer(Year.class, YearSerializer.class);
//		if (isClassAvailable("java.time.YearMonth")) kryo.addDefaultSerializer(YearMonth.class, YearMonthSerializer.class);
//		if (isClassAvailable("java.time.MonthDay")) kryo.addDefaultSerializer(MonthDay.class, MonthDaySerializer.class);
//		if (isClassAvailable("java.time.Period")) kryo.addDefaultSerializer(Period.class, PeriodSerializer.class);		
		
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
