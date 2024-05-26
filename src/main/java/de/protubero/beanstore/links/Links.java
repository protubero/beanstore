package de.protubero.beanstore.links;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;

public interface Links extends Iterable<Link<?, ?>>, Consumer<Consumer<Link<?, ?>>> {

	public static Links EMPTY = new Links() {
		
		
		@Override
		public Iterator<Link<?, ?>> iterator() {
			return Collections.emptyIterator();
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

		@Override
		public void accept(Consumer<Link<?, ?>> t) {}
		
	};
	
	
	Link<?, ?> oneOf(String type);
	Link<?, ?> oneInboundOf(String type);
	Link<?, ?> oneOutboundOf(String type);
	
	Links remove(LinkObj<?, ?> linkObj);
	
	void checkRelationTo(String alias, Long id);
	
}
