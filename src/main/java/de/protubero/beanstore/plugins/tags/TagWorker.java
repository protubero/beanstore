package de.protubero.beanstore.plugins.tags;

import java.util.Objects;
import java.util.Optional;

public class TagWorker {

	private Tagged taggedEntity;
	
	private TagWorker(Tagged aTaggedEntity) {
		this.taggedEntity = Objects.requireNonNull(aTaggedEntity);
	}
	
	public static TagWorker of(Tagged aTaggedEntity) {
		return new TagWorker(aTaggedEntity);
	}

	public TagWorker plus(Tag aTag) {
		Tags newTags = taggedEntity.getTags().plus(aTag);
		taggedEntity.setTags(newTags);
		return this;
	}

	public TagWorker plus(Tag ... tags) {
		Tags newTags = taggedEntity.getTags().plus(tags);
		taggedEntity.setTags(newTags);
		return this;
	}
	
	public TagWorker minus(Tag aTag) {
		Tags newTags = taggedEntity.getTags().minus(aTag);
		taggedEntity.setTags(newTags);
		return this;
	}
	
	public TagWorker minus(Tag ... tags) {
		Tags newTags = taggedEntity.getTags().minus(tags);
		taggedEntity.setTags(newTags);
		return this;
	}
	
	public boolean contains(Tag tag) {
		return taggedEntity.getTags().contains(tag);
	}

	public Optional<Tag> findFirst(TagGroup group) {
		return taggedEntity.getTags().findFirst(group);
	}

	
}
