package de.protubero.beanstore.plugins.tags;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.protubero.beanstore.base.entity.AbstractEntity;

public class AbstractTaggedEntity extends AbstractEntity implements Tagged {

	@JsonProperty("tags")
	private Tags tags = new Tags();
	
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
