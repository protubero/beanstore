package de.protubero.beanstore.model;

import com.tngtech.archunit.thirdparty.com.google.common.base.Charsets;

import de.protubero.beanstore.persistence.api.CustomValueType;

public class PostCode implements CustomValueType {

	private String code;
	
	
	public PostCode() {
	}
	
	public PostCode(byte[] byteArray) {
		if (byteArray != null) {
			code = new String(byteArray, Charsets.UTF_8);			
		}
	}

	public PostCode(String aCode) {
		this.code = aCode;
	}

	public String getCode() {
		return code;
	}

	@Override
	public void bytes(byte[] aByteArray) {
		if (aByteArray != null) {
			code = new String(aByteArray, Charsets.UTF_8);			
		}
	}
	
	@Override
	public byte[] bytes() {
		if (code == null) {
			return null;
		}
		return code.getBytes(Charsets.UTF_8);
	}
	
}
