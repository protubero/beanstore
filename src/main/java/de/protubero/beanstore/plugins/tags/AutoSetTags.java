package de.protubero.beanstore.plugins.tags;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AutoSetTags {

	private Tagged tagged;
	private Tags tags;

	public AutoSetTags(Tagged tagged, Tags tags) {
		this.tagged = Objects.requireNonNull(tagged);
		this.tags = Objects.requireNonNull(tags);
	}

	public Tagged getTagged() {
		return tagged;
	}

	public Tags getTags() {
		return tags;
	}
	
	public int size() {
		return tags.size();
	}
	
	public AutoSetTags plus(Tag aTag) {
		return new AutoSetTags(tagged, tags.plus(aTag));
	}

	public AutoSetTags plus(Tag ... tags) {
		return new AutoSetTags(tagged, tags.plusAll(List.of(tags)));
	}
	
	public AutoSetTags minus(Tag aTag) {
		return new AutoSetTags(tagged, tags.minus(aTag));
	}
	
	public AutoSetTags minus(Tag ... tags) {
		return new AutoSetTags(tagged, tags.minusAll(tags));
	}
	
	@Override
	public String toString() {
		return tags.toString();
	}

	public boolean contains(Tag tag) {
		return tags.contains(tag);
	}

	public Optional<Tag> findFirst(TagGroup group) {
		return tags.findFirst(group);
	}
	
	
}
