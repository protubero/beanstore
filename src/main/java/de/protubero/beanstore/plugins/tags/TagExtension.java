package de.protubero.beanstore.plugins.tags;

import de.protubero.beanstore.api.BeanStoreFactory;

public class TagExtension {

	public static void init(BeanStoreFactory beanStoreFactory) {
		beanStoreFactory.kryoConfig().register(Tag.class, new TagSerializer(), 2626);
		beanStoreFactory.kryoConfig().register(Tags.class, new TagsSerializer(), 2627);
	}
	
}