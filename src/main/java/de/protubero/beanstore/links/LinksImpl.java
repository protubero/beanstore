package de.protubero.beanstore.links;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import de.protubero.beanstore.entity.PersistentObjectKey;

public class LinksImpl implements Links {

	private PersistentObjectKey<?> ownerKey;
	private PSet<Link<?, ?>> linkSet;
	
	LinksImpl(PersistentObjectKey<?> ownerKey) {
		this.ownerKey = Objects.requireNonNull(ownerKey);
	}
	
	public LinksImpl(PersistentObjectKey<?> ownerKey, PSet<Link<?, ?>> linkSet) {
		this(ownerKey);
		this.linkSet = Objects.requireNonNull(linkSet);
	}

	@Override
	public Iterator<Link<?, ?>> iterator() {
		if (linkSet == null) {
			return Collections.emptyIterator();
		} else {
			return linkSet.iterator();
		}
	}

	@Override
	public PersistentObjectKey<?> ownerKey() {
		return ownerKey;
	}

	@Override
	public Links plus(Link<?, ?> aLink) {
		if (linkSet == null) {
			linkSet = HashTreePSet.empty(); 
		}
		
		PSet<Link<?, ?>> newLinkSet = linkSet.plus(aLink);
		if (newLinkSet.size() != linkSet.size() + 1) {
			throw new AssertionError();
		}
		return new LinksImpl(ownerKey, newLinkSet);
	}

	@Override
	public Links minus(Link<?, ?> aLink) {
		if (linkSet == null) {
			throw new AssertionError();
		}
		
		PSet<Link<?, ?>> result = linkSet.minus(Objects.requireNonNull(aLink));
		if (result.size() != linkSet.size() - 1) {
			throw new AssertionError();
		}
		return new LinksImpl(ownerKey, result);
	}

	@Override
	public Stream<Link<?, ?>> stream() {
		return linkSet.stream();
	}

	@Override
	public Link<?, ?> findByLinkObj(final LinkObj<?, ?> linkObj) {
		return stream().filter(l -> l.getLinkObj().equals(linkObj)).findAny().orElse(null);
	}

}
