package de.protubero.beanstore.model;

import de.protubero.beanstore.persistence.api.PropertyBean;

@PropertyBean(300)
public class PostCode  {

	private String code;
	
	
	public PostCode() {
	}
	

	public PostCode(String aCode) {
		this.code = aCode;
	}

	public String getCode() {
		return code;
	}

	
	
}
