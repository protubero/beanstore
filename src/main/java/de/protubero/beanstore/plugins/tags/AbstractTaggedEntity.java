package de.protubero.beanstore.plugins.tags;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.protubero.beanstore.base.entity.AbstractEntity;

public class AbstractTaggedEntity extends AbstractEntity implements Tagged {

	@JsonProperty("tags")
	private Tags tags;
	
	@Override
	public Tags getTags() {
		if (tags == null) {
			return Tags.EMPTY;
		} else {
			return tags;
		}
	}

	@Override
	public void setTags(Tags tags) {
		this.tags = tags;
	}
	
	public void tagWith(Tag aTag) {
		Tags newTags = getTags().plus(aTag);
		setTags(newTags);
	}

	public void tagWith(Tag ... tags) {
		Tags newTags = getTags().plus(tags);
		setTags(newTags);
	}
	
	public void unTag(Tag aTag) {
		Tags newTags = getTags().minus(aTag);
		setTags(newTags);
	}
	
	public void unTag(Tag ... tags) {
		Tags newTags = getTags().minus(tags);
		setTags(newTags);
	}
	
}
