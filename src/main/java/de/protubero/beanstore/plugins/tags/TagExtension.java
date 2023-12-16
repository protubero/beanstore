package de.protubero.beanstore.plugins.tags;

import de.protubero.beanstore.api.BeanStoreFactory;

public class TagExtension {

	public static void init(BeanStoreFactory beanStoreFactory) {
		beanStoreFactory.register(Tag.class, new TagSerializer(), 2626);
		beanStoreFactory.register(Tags.class, new TagsSerializer(), 2627);
	}
	
}
