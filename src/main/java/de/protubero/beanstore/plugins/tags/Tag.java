package de.protubero.beanstore.plugins.tags;

import java.util.Objects;

public final class Tag {

	private TagGroup group;
	private String name;
	private boolean registered;

	Tag(TagGroup group, String name, boolean registered) {
		TagNameChecker.throwIfContainsInvalidChar(name);

		this.group = Objects.requireNonNull(group);
		this.name = Objects.requireNonNull(name);		
		this.registered = registered;
	}
	
	public TagGroup group() {
		return group;
	}
	
	public String name() {
		return name;
	}
	
	public boolean registered() {
		return registered;
	}
	
	@Override
	public String toString() {
		return group.name() + ":" + name;
	}

}
