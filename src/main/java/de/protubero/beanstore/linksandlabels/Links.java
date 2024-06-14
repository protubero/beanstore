package de.protubero.beanstore.linksandlabels;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Links {

	Stream<Link<?, ?>> stream();

	void to(String alias, long id, Consumer<Link<?, ?>> consumer);
	
	
}
