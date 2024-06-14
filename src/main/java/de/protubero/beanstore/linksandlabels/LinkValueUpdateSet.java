package de.protubero.beanstore.linksandlabels;

import java.util.Collection;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

public final class LinkValueUpdateSet extends UpdatePSet<LinkValue> {

	public LinkValueUpdateSet() {
		super(HashTreePSet.empty(), HashTreePSet.empty());
	}

	public LinkValueUpdateSet(PSet<LinkValue> posSet, PSet<LinkValue> negSet) {
		super(posSet, negSet);
	}

	public static LinkValueUpdateSet empty() {
		return new LinkValueUpdateSet();
	}
	@Override
	public PSet<LinkValue> plus(LinkValue e) {
		return new LinkValueUpdateSet(getPositiveSet().plus(e), getNegativeSet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public PSet<LinkValue> plusAll(Collection<? extends LinkValue> list) {
		return new LinkValueUpdateSet(getPositiveSet().plusAll((Collection<LinkValue>) list), getNegativeSet());
	}

	@Override
	public PSet<LinkValue> minus(Object e) {
		return new LinkValueUpdateSet(getPositiveSet(), getNegativeSet().plus((LinkValue) e));
	}

	@SuppressWarnings("unchecked")
	@Override
	public PSet<LinkValue> minusAll(Collection<?> list) {
		return new LinkValueUpdateSet(getPositiveSet(), getNegativeSet().plusAll((Collection<LinkValue>) list));
	}

	
}
