package de.protubero.beanstore.persistence;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.protubero.beanstore.builder.BeanStoreBuilder;
import de.protubero.beanstore.model.KryoTestEntity;
import de.protubero.beanstore.persistence.kryo.KryoConfiguration;
import de.protubero.beanstore.persistence.kryo.KryoPersistence;

public class KryoRegistrationTest {
	
	@TempDir
	File pFileDir;	
	
	@Test
	public void test() throws MalformedURLException {
		KryoConfiguration kryoConf = KryoConfiguration.create();
		kryoConf.registerDefaultArrays();
		
		BeanStoreBuilder builder = BeanStoreBuilder.init(KryoPersistence.of(new File(pFileDir, "beanstore.kryo"), kryoConf));
		builder.registerEntity(KryoTestEntity.class);
		var store = builder.build();
		var tx = store.transaction();
		var newObj = tx.create(KryoTestEntity.class);
		
		Instant now = Instant.now();
		
		// java.lang
		newObj.setLongValue(20l);
		newObj.setIntValue(44);
		newObj.setByteValue(Byte.MAX_VALUE);
		newObj.setFloatValue(3.4f);
		newObj.setShortValue(Short.MAX_VALUE);
		newObj.setDoubleValue(5.6d);
		newObj.setBooleanValue(false);
		newObj.setCharValue('D');
		newObj.setStringValue("AnyText");
		
		// java.time
		newObj.setInstantValue(now);
		newObj.setDuration(java.time.Duration.ofDays(20));
		newObj.setLocalDate(LocalDate.now());
		newObj.setLocalTime(LocalTime.now());
		newObj.setLocalDateTime(LocalDateTime.now());
		newObj.setZoneOffset(ZoneOffset.MAX);
		newObj.setZoneId(ZoneId.of("Z"));
		newObj.setOffsetTime(OffsetTime.now());
		newObj.setOffsetDateTime(OffsetDateTime.now());
		newObj.setZonedDateTime(ZonedDateTime.now());
		newObj.setYear(Year.now());
		newObj.setYearMonth(YearMonth.now());
		newObj.setMonthDay(MonthDay.now());
		newObj.setPeriod(Period.ofDays(3));
		newObj.setMonth(Month.APRIL);
		newObj.setDayOfWeek(DayOfWeek.FRIDAY);
		
		
		// arrays
		newObj.setByteArray(new byte[]{(byte)4});
		newObj.setIntArray(new int[]{4});
		newObj.setLongArray(new long[]{4l});
		newObj.setCharArray(new char[]{(byte) 4});
		newObj.setBooleanArray(new boolean[]{true, false});
		newObj.setFloatArray(new float[]{3.4f});
		newObj.setDoubleArray(new double[]{3.4d});
		newObj.setShortArray(new short[]{3});
		newObj.setStringArray(new String[]{"hello", "world"});

		// java.math
		newObj.setBigDecimal(new BigDecimal("313214143.321323"));
		newObj.setBigInteger(new BigInteger("33333333333333"));
		newObj.setRoundingMode(RoundingMode.DOWN);
		
		// java.util
		newObj.setDate(new Date());
		newObj.setCurrency(Currency.getInstance("USD"));
		newObj.setLocale(Locale.FRANCE);
		
		// java.net
		newObj.setUrl(new URL("http://www.heise.de:89/test?a=b"));
		
		tx.execute();
		store.close();
		
		BeanStoreBuilder readBuilder = BeanStoreBuilder.init(KryoPersistence.of(new File(pFileDir, "beanstore.kryo"), kryoConf));
		readBuilder.registerEntity(KryoTestEntity.class);
		var readStore = readBuilder.build();

		var kryoStore = readStore.snapshot().entity(KryoTestEntity.class);
		assertEquals(1, kryoStore.count());
		var readObj = kryoStore.stream().findFirst().get();
		
		assertEquals(newObj.getLongValue(), readObj.getLongValue());
		assertEquals(newObj.getIntValue(), readObj.getIntValue());
		assertEquals(newObj.getByteValue(), readObj.getByteValue());
		assertEquals(newObj.getFloatValue(), readObj.getFloatValue());
		assertEquals(newObj.getShortValue(), readObj.getShortValue());
		assertEquals(newObj.getDoubleValue(), readObj.getDoubleValue());
		assertEquals(newObj.getBooleanValue(), readObj.getBooleanValue());
		assertEquals(newObj.getCharValue(), readObj.getCharValue());
		assertEquals(newObj.getStringValue(), readObj.getStringValue());

		assertEquals(newObj.getInstantValue(), readObj.getInstantValue());
		assertEquals(newObj.getDuration(), readObj.getDuration());
		assertEquals(newObj.getLocalDate(), readObj.getLocalDate());
		assertEquals(newObj.getLocalTime(), readObj.getLocalTime());
		assertEquals(newObj.getLocalDateTime(), readObj.getLocalDateTime());
		assertEquals(newObj.getZoneOffset(), readObj.getZoneOffset());
		assertEquals(newObj.getZoneId(), readObj.getZoneId());
		assertEquals(newObj.getOffsetTime(), readObj.getOffsetTime());
		assertEquals(newObj.getOffsetDateTime(), readObj.getOffsetDateTime());
		assertEquals(newObj.getZonedDateTime(), readObj.getZonedDateTime());
		assertEquals(newObj.getYear(), readObj.getYear());
		assertEquals(newObj.getYearMonth(), readObj.getYearMonth());
		assertEquals(newObj.getMonthDay(), readObj.getMonthDay());
		assertEquals(newObj.getPeriod(), readObj.getPeriod());
		assertEquals(newObj.getMonth(), readObj.getMonth());
		assertEquals(newObj.getDayOfWeek(), readObj.getDayOfWeek());
		
		
		assertArrayEquals(newObj.getByteArray(), readObj.getByteArray());
		assertArrayEquals(newObj.getIntArray(), readObj.getIntArray());
		assertArrayEquals(newObj.getStringArray(), readObj.getStringArray());
		assertArrayEquals(newObj.getLongArray(), readObj.getLongArray());
		assertArrayEquals(newObj.getCharArray(), readObj.getCharArray());
		assertArrayEquals(newObj.getShortArray(), readObj.getShortArray());
		assertArrayEquals(newObj.getBooleanArray(), readObj.getBooleanArray());
		assertArrayEquals(newObj.getByteArray(), readObj.getByteArray());
		
		assertEquals(newObj.getBigInteger(), readObj.getBigInteger());
		assertEquals(newObj.getBigDecimal(), readObj.getBigDecimal());
		assertEquals(newObj.getRoundingMode(), readObj.getRoundingMode());
		
		assertEquals(newObj.getDate(), readObj.getDate());
		assertEquals(newObj.getCurrency(), readObj.getCurrency());		
		assertEquals(newObj.getLocale(), readObj.getLocale());
		
		assertEquals(newObj.getUrl(), readObj.getUrl());
		
	}
}
