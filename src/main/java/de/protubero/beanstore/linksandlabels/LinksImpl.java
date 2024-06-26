package de.protubero.beanstore.linksandlabels;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

public class LinksImpl implements Links {

	private PSet<Link<?, ?>> linkSet; 
	
	LinksImpl(PSet<Link<?, ?>> linkSet) {
		this.linkSet = linkSet;
	}
	
	public LinksImpl() {
		linkSet = HashTreePSet.empty();
	}

	public LinksImpl(Collection<Link<?, ?>> linkList) {
		this.linkSet = HashTreePSet.from(linkList);
	}

	@Override
	public Stream<Link<?, ?>> stream() {
		return linkSet.stream();
	}
	
	public LinksImpl plus(Link<?, ?> aLink) {
		return new LinksImpl(linkSet.plus(aLink)); 
	}

	public LinksImpl minus(Link<?, ?> aLink) {
		return new LinksImpl(linkSet.minus(aLink)); 
	}

	@Override
	public void to(String alias, long id, Consumer<Link<?, ?>> consumer) {
		stream().filter(link -> link.target().id().longValue() == id 
				&& link.target().alias().equals(alias)).forEach(consumer);
	}
}
