package de.protubero.beanstore.persistence.base;

import java.util.Optional;
import java.util.Set;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

public class TagGroup {

	private PSet<Tag> tagSet = HashTreePSet.empty();
	private PMap<String, Tag> tagMap = HashTreePMap.empty();
	private String name;
	private boolean registered;
	
	TagGroup(String name, boolean registered) {
		TagNameChecker.throwIfContainsInvalidChar(name);
		
		this.name = name;
		this.registered = registered;
	}
	
	public boolean registered() {
		return registered;
	}

	public Tag newTag(String name) {
		Tag newTag = new Tag(this, name, true);
		if (tagMap.containsKey(name)) {
			throw new RuntimeException("Duplicate tag: " + newTag);
		}
		tagSet = tagSet.plus(newTag);
		tagMap = tagMap.plus(name, newTag);
		
		return newTag;
	}
	
	public Tag tag(String tagName) {
		Tag tag = tagMap.get(tagName);
		if (tag == null) {
			throw new RuntimeException("Tag not found: " + name + ":" + tagName);
		}
		return tag;
	}
	
	synchronized Tag tagAuto(String name) {
		Tag tag = tagMap.get(name);
		if (tag == null) {
			tag = new Tag(this, name, false);
			tagMap = tagMap.plus(name, tag);
			tagSet = tagSet.plus(tag);
		}
		return tag;
	}
	
	
	public Optional<Tag> tagOptional(String tagName) {
		return Optional.ofNullable(tagMap.get(tagName));
	}
	
	public Set<Tag> tagSet() {
		return tagSet;
	}
	
	public String name() {
		return name;
	}

	void registered(boolean aRegistered) {
		registered = aRegistered;
	}
	
}
