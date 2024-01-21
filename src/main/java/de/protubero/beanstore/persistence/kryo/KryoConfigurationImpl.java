package de.protubero.beanstore.persistence.kryo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.KryoSerializable;
import com.esotericsoftware.kryo.kryo5.Registration;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.BooleanArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.ByteArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.CharArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.DoubleArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.FloatArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.IntArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.LongArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.ShortArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultArraySerializers.StringArraySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers.BigDecimalSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers.BigIntegerSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers.CurrencySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers.DateSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers.KryoSerializableSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers.LocaleSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers.URLSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.DurationSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.InstantSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.LocalDateSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.LocalDateTimeSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.LocalTimeSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.MonthDaySerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.OffsetDateTimeSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.OffsetTimeSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.PeriodSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.YearMonthSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.YearSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.ZoneIdSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.ZoneOffsetSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.TimeSerializers.ZonedDateTimeSerializer;

import de.protubero.beanstore.persistence.api.PersistenceException;
import de.protubero.beanstore.persistence.api.PersistentTransaction;

public class KryoConfigurationImpl implements KryoConfiguration {

	public static final Logger log = LoggerFactory.getLogger(KryoConfigurationImpl.class);
	
	private Kryo kryo;

	private boolean locked;

	private KryoDictionary dictionary = new KryoDictionary(); 
	
	public KryoConfigurationImpl() {
		kryo = new Kryo();
		
		kryo.setRegistrationRequired(true);
		kryo.setWarnUnregisteredClasses(true);
		kryo.setAutoReset(true);
		kryo.setReferences(false);
		
		// java.lang
		// Add RecordSerializer if JDK 14+ available
//		if (isClassAvailable("java.lang.Record")) {
//			addDefaultSerializer("java.lang.Record", RecordSerializer.class);
//		}		
		
		// java.math
		kryo.register(BigInteger.class, new BigIntegerSerializer(), 19);
		kryo.register(BigDecimal.class, new BigDecimalSerializer(), 20);
		// RoundingMode enum
		
		// java.net
		kryo.register(URL.class, new URLSerializer(), 27);
		// URL
		// Inet4Address
		// Inet6Address
		
		// java.util
		kryo.register(Locale.class, new LocaleSerializer(), 25);
		kryo.register(Date.class, new DateSerializer(), 21);
		kryo.register(Currency.class, new CurrencySerializer(), 22);
		
		// java.time
		kryo.register(Instant.class, new InstantSerializer(), 29);
		kryo.register(Duration.class, new DurationSerializer(), 30);
		kryo.register(LocalDateTime.class, new LocalDateTimeSerializer(), 31);
		kryo.register(LocalDate.class, new LocalDateSerializer(), 32);
		kryo.register(LocalTime.class, new LocalTimeSerializer(), 33);
		kryo.register(ZoneOffset.class, new ZoneOffsetSerializer(), 34);
		kryo.register(ZoneId.class, new ZoneIdSerializer(), 35);
		kryo.register(OffsetTime.class, new OffsetTimeSerializer(), 36);
		kryo.register(OffsetDateTime.class, new OffsetDateTimeSerializer(), 37);
		kryo.register(ZonedDateTime.class, new ZonedDateTimeSerializer(), 38);		
		kryo.register(Year.class, new YearSerializer(), 39);
		kryo.register(YearMonth.class, new YearMonthSerializer(), 40);
		kryo.register(MonthDay.class, new MonthDaySerializer(), 41);
		kryo.register(Period.class, new PeriodSerializer(), 42);
		// DayOfWeek
		// Month
		
		kryo.register(PersistentTransaction.class, new PersistentTransactionSerializer(dictionary),  99);
	}

	@Override
	public void registerDefaultArrays() {
		kryo.register(byte[].class, new ByteArraySerializer(), 10);
		kryo.register(char[].class, new CharArraySerializer(), 11);
		kryo.register(short[].class, new ShortArraySerializer(), 12);
		kryo.register(int[].class, new IntArraySerializer(), 13);
		kryo.register(long[].class, new LongArraySerializer(), 14);
		kryo.register(float[].class, new FloatArraySerializer(), 15);
		kryo.register(double[].class, new DoubleArraySerializer(), 16);
		kryo.register(boolean[].class, new BooleanArraySerializer(), 17);
		kryo.register(String[].class, new StringArraySerializer(), 18);
	}
	
	@Override
	public void register(Class<?> propertyBeanClass) {
		if (locked) {
			throw new RuntimeException("Kryo configuration is already locked");
		}
		
		KryoId pbAnnotation = propertyBeanClass.getAnnotation(KryoId.class);
		if (pbAnnotation == null) {
			throw new RuntimeException("Property bean classes must be annotated with PropertyBean annotation");
		}
		
		int serializationId = pbAnnotation.value();
		
		log.info("Registering property bean class " + propertyBeanClass + "[" + serializationId + "]");
		if (serializationId < 100) {
			throw new PersistenceException("Invalid kryo id, should be >= 100: " + serializationId);
		}
		
		if (KryoSerializable.class.isAssignableFrom(propertyBeanClass)) {
			kryo.register(propertyBeanClass, new KryoSerializableSerializer(), serializationId);
		} else {
			kryo.register(propertyBeanClass, new PropertyBeanSerializer(kryo, propertyBeanClass), serializationId);
		}	
	}
	
	@Override
	public <T> Registration register(Class<T> type, Serializer<T> serializer, int id) {
		if (locked) {
			throw new RuntimeException("Kryo configuration is already locked");
		}

		if (id < 100) {
			throw new PersistenceException("Invalid kryo id, should be >= 100: " + id);
		}
		return kryo.register(type, serializer, id);
	}

	Kryo getKryo() {
		return kryo;
	}

	public void lock() {
		this.locked = true;
	}

	KryoDictionary getDictionary() {
		return dictionary;
	}

	
}
