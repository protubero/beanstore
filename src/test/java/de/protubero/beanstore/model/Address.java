package de.protubero.beanstore.model;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.Entity;

@Entity(alias = "address")
public class Address extends AbstractEntity {

	private String street;
	private String city;
	
	private PostCode postCode;

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public PostCode getPostCode() {
		return postCode;
	}

	public void setPostCode(PostCode postCode) {
		this.postCode = postCode;
	} 

	
}
