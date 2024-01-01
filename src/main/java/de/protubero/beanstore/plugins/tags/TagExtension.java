package de.protubero.beanstore.plugins.tags;

import de.protubero.beanstore.factory.BeanStoreFactory;

public class TagExtension {

	public static void init(BeanStoreFactory beanStoreFactory) {
		beanStoreFactory.registerKryoSerializer(Tag.class, new TagSerializer(), 2626);
		beanStoreFactory.registerKryoSerializer(Tags.class, new TagsSerializer(), 2627);
	}
	
}
