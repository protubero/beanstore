package de.protubero.beanstore.plugins.tags;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

public final class Tags {
		
	public static final Tags EMPTY = new Tags();
	
	PSet<Tag> tagSet;
	
	public Tags() {
		tagSet = HashTreePSet.empty();		
	}
	
	Tags(PSet<Tag> aTagSet) {
		this.tagSet = Objects.requireNonNull(aTagSet);
	}

	public int size() {
		return tagSet.size();
	}
	
	public Tags plus(Tag aTag) {
		return new Tags(tagSet.plus(aTag));
	}

	public Tags plus(Tag ... tags) {
		return new Tags(tagSet.plusAll(List.of(tags)));
	}
	
	public Tags minus(Tag aTag) {
		return new Tags(tagSet.minus(aTag));
	}
	
	public Tags minus(Tag ... tags) {
		return new Tags(tagSet.minusAll(List.of(tags)));
	}
	
	@Override
	public String toString() {
		return tagSet.toString();
	}

	public boolean contains(Tag tag) {
		return tagSet.contains(tag);
	}

	public Optional<Tag> findFirst(TagGroup group) {
		return tagSet.stream().filter(tag -> tag.group() == group).findFirst();
	}

}
