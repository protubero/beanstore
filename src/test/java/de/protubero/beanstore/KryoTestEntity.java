package de.protubero.beanstore;

import java.time.Instant;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.Entity;

@Entity(alias = "kryotest")
public class KryoTestEntity extends AbstractEntity {
	
	private Long longValue;
	private Integer intValue;
	private Byte byteValue;
	private Short shortValue;
	private Float floatValue;
	private Double doubleValue;
	private Boolean booleanValue;
	private Character charValue;

	private String stringValue;
	
	private Instant instantValue;

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	public Byte getByteValue() {
		return byteValue;
	}

	public void setByteValue(Byte byteValue) {
		this.byteValue = byteValue;
	}

	public Short getShortValue() {
		return shortValue;
	}

	public void setShortValue(Short shortValue) {
		this.shortValue = shortValue;
	}

	public Float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(Float floatValue) {
		this.floatValue = floatValue;
	}

	public Double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public Character getCharValue() {
		return charValue;
	}

	public void setCharValue(Character charValue) {
		this.charValue = charValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Instant getInstantValue() {
		return instantValue;
	}

	public void setInstantValue(Instant instantValue) {
		this.instantValue = instantValue;
	}
	
}