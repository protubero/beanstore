package de.protubero.beanstore.plugins.tags;

import java.util.Optional;
import java.util.Set;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

public final class TagManager {

	private PSet<TagGroup> groupSet = HashTreePSet.empty();
	private PMap<String, TagGroup> groupMap = HashTreePMap.empty();


	private static TagManager INSTANCE = new TagManager();
	
	
	private TagManager() {
	}
	
	public static TagManager instance() {
		return INSTANCE;
	}
	
	public synchronized TagGroup newGroup(String name) {
		TagGroup group = new TagGroup(name, true);
		
		TagGroup existingGroup = groupMap.get(name);
		if (existingGroup != null) {			
			if (existingGroup.registered()) {
				throw new RuntimeException("Duplicate tag group: " + name);
			} else {
				existingGroup.registered(true);
				return existingGroup;
			}
		} else {	
			groupMap = groupMap.plus(name, group);
			groupSet = groupSet.plus(group);
			return group;
		}
	}

	public synchronized TagGroup groupAuto(String name) {
		TagGroup group = groupMap.get(name);
		if (group == null) {
			group = new TagGroup(name, false);
			groupMap = groupMap.plus(name, group);
			groupSet = groupSet.plus(group);
		}
		return group;
	}
	
	public TagGroup group(String name) {
		TagGroup group = groupMap.get(name);
		if (group == null) {
			throw new RuntimeException("Invalid tag group name: " + name);
		}
		return group;
	}
	
	public Optional<TagGroup> groupOptional(String name) {
		return Optional.ofNullable(groupMap.get(name));
	}
	
	public Set<TagGroup> groupSet() {
		return groupSet;
	}
	
}
