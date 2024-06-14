package de.protubero.beanstore.persistence.kryo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
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
import java.util.Objects;

import org.pcollections.MapPSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.kryo5.Kryo;
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
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers.LocaleSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers.URISerializer;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers.URLSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.EnumNameSerializer;
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

import de.protubero.beanstore.entity.PersistentObjectKeyImpl;
import de.protubero.beanstore.linksandlabels.LabelUpdateSet;
import de.protubero.beanstore.linksandlabels.LabelUpdateSetKryoSerializer;
import de.protubero.beanstore.linksandlabels.LinkValueUpdateSet;
import de.protubero.beanstore.linksandlabels.LinkValueUpdateSetKryoSerializer;
import de.protubero.beanstore.linksandlabels.MapPSetKryoSerializer;
import de.protubero.beanstore.linksandlabels.PersistentObjectKeyKryoSerializer;
import de.protubero.beanstore.persistence.api.KryoConfig;
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
				
		// java.math
		kryo.register(BigInteger.class, new BigIntegerSerializer(), 19);
		kryo.register(BigDecimal.class, new BigDecimalSerializer(), 20);
		kryo.register(RoundingMode.class, new EnumNameSerializer(RoundingMode.class), 21);
		
		// java.util
		kryo.register(Currency.class, new CurrencySerializer(), 22);
		kryo.register(Locale.class, new LocaleSerializer(), 23);
		kryo.register(Date.class, new DateSerializer(), 24);

		// java.net
		kryo.register(URL.class, new URLSerializer(), 25);
		kryo.register(URI.class, new URISerializer(), 26);
		
		
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
		kryo.register(DayOfWeek.class, new EnumNameSerializer(DayOfWeek.class), 43);
		kryo.register(Month.class, new EnumNameSerializer(Month.class), 44);
		

		kryo.register(byte[].class, new ByteArraySerializer(), 70);
		kryo.register(char[].class, new CharArraySerializer(), 71);
		kryo.register(short[].class, new ShortArraySerializer(), 72);
		kryo.register(int[].class, new IntArraySerializer(), 73);
		kryo.register(long[].class, new LongArraySerializer(), 74);
		kryo.register(float[].class, new FloatArraySerializer(), 75);
		kryo.register(double[].class, new DoubleArraySerializer(), 76);
		kryo.register(boolean[].class, new BooleanArraySerializer(), 77);
		kryo.register(String[].class, new StringArraySerializer(), 78);

		kryo.register(MapPSet.class, new MapPSetKryoSerializer(), 80);
		kryo.register(PersistentObjectKeyImpl.class, new PersistentObjectKeyKryoSerializer(dictionary), 81);
		kryo.register(LinkValueUpdateSet.class, new LinkValueUpdateSetKryoSerializer(dictionary), 82);
		kryo.register(LabelUpdateSet.class, new LabelUpdateSetKryoSerializer(dictionary), 83);
		
		
		kryo.register(PersistentTransaction.class, new PersistentTransactionSerializer(dictionary),  99);
	}
	
	
	@Override
	public <T> Registration register(Class<T> type, Serializer<T> serializer, int id) {
		if (locked) {
			throw new RuntimeException("Kryo configuration is already locked");
		}

		if (id < 200) {
			throw new PersistenceException("Invalid kryo id, should be >= 200: " + id);
		}
		
		if (serializer instanceof DictionaryUsing) {
			((DictionaryUsing) serializer).setDictionary(dictionary);
		}
		
		return kryo.register(type, serializer, id);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Registration register(Class<T> type, Class<? extends Serializer> serializerClass, int id) {
		Objects.requireNonNull(serializerClass);
		
		Constructor<? extends Serializer<T>> serializerConstructor;
		Serializer<T> serializer = null;
		try {
			try {
				serializerConstructor = (Constructor<? extends Serializer<T>>) serializerClass.getConstructor(Class.class);
				serializer = serializerConstructor.newInstance(type);
			} catch (NoSuchMethodException e) {
				try {
					serializerConstructor = (Constructor<? extends Serializer<T>>) serializerClass.getConstructor();
					serializer = serializerConstructor.newInstance();
				} catch (NoSuchMethodException e2) {
					throw new RuntimeException(
							"Missing no-arg constructor of serializer " + serializerClass.getSimpleName());
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		return register(type, serializer, id);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Registration register(Class<T> type) {
		log.info("Kryo configuration of class {}", type);

		KryoConfig annotation = type.getAnnotation(KryoConfig.class);

		Constructor<? extends Serializer> serializerConstructor;
		Serializer<?> serializer = null;
		try {
			try {
				serializerConstructor = annotation.serializer().getConstructor(Class.class);
				serializer = serializerConstructor.newInstance(type);
			} catch (NoSuchMethodException e) {
				try {
					serializerConstructor = annotation.serializer().getConstructor();
					serializer = serializerConstructor.newInstance();
				} catch (NoSuchMethodException e2) {
					throw new RuntimeException(
							"Missing no-arg constructor of serializer " + annotation.serializer().getSimpleName());
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return register(type, (Serializer) serializer, annotation.id());
	}


	
}
