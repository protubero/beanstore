package de.protubero.beanstore.base.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbstractTaggedEntity extends AbstractEntity {

	@JsonProperty("tags")
	private Tags tags = new Tags(this);
	
	public Tags getTags() {
		return tags;
	}

	public void setTags(Tags tags) {
		if (tags == null) {
			throw new RuntimeException("It is not allowed to set tags to null");
		}
		this.tags = tags;
	}

	
	
}
