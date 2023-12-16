package de.protubero.beanstore.plugins.tags;

public interface Tagged {

	static final Tags EMPTY_TAGS = new Tags();
	
	Tags getTags();

	void setTags(Tags tags);
	
	default Tags nonNullTags() {
		Tags tags = getTags();
		if (tags == null) {
			return EMPTY_TAGS;
		} else {
			return tags;
		}
	}
	
	default AutoSetTags autoSetTags() {
	}
}
