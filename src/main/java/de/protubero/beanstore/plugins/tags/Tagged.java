package de.protubero.beanstore.plugins.tags;

public interface Tagged {

	static final Tags EMPTY_TAGS = new Tags();
	
	Tags getTags();

	void setTags(Tags tags);
	
}
