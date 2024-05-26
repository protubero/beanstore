package de.protubero.beanstore.links;

import java.util.Iterator;
import java.util.function.Consumer;

public class LinksImpl implements Links {

	private Link<?, ?>[] links;
 
	@Override
	public Iterator<Link<?, ?>> iterator() {
		return null;
	}

	@Override
	public void accept(Consumer<Link<?, ?>> t) {
		
	}

	@Override
	public Link<?, ?> oneOf(String type) {
		return null;
	}

	@Override
	public Link<?, ?> oneInboundOf(String type) {
		return null;
	}

	@Override
	public Link<?, ?> oneOutboundOf(String type) {
		return null;
	}
	

}
