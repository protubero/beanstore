package de.protubero.beanstore.plugins.tags;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.protubero.beanstore.entity.AbstractEntity;

public class AbstractTaggedEntity extends AbstractEntity implements Tagged {

	@JsonProperty("tags")
	private Tags tags;
	
	@Override
	public Tags getTags() {
		return tags;
	}

	public Tags tagsOrEmpty() {
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
		Tags newTags = tagsOrEmpty().plus(aTag);
		setTags(newTags);
	}

	public void tagWith(Tag ... tags) {
		Tags newTags = tagsOrEmpty().plus(tags);
		setTags(newTags);
	}
	
	public void unTag(Tag aTag) {
		Tags newTags = tagsOrEmpty().minus(aTag);
		setTags(newTags);
	}
	
	public void unTag(Tag ... tags) {
		Tags newTags = tagsOrEmpty().minus(tags);
		setTags(newTags);
	}
	
}
