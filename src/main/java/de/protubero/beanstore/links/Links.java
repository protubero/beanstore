package de.protubero.beanstore.links;

import java.util.stream.Stream;

import de.protubero.beanstore.entity.PersistentObjectKey;

public interface Links extends Iterable<Link<?, ?>> {

	public static Links empty(PersistentObjectKey<?> ownerKey) { 
		return new LinksImpl(ownerKey);	
	}
	

	PersistentObjectKey<?> ownerKey();
	
	Links plus(Link<?, ?> aLink);
		
	Links minus(Link<?, ?> aLink);
		
	Stream<Link<?, ?>> stream();


	Link<?, ?> findByLinkObj(LinkObj<?, ?> linkObj);
}
