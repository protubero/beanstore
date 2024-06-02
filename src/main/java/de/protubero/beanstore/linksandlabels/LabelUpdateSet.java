package de.protubero.beanstore.linksandlabels;

import java.util.Collection;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

public final class LabelUpdateSet extends UpdatePSet<String> {

	public LabelUpdateSet() {
		super(HashTreePSet.empty(), HashTreePSet.empty());
	}

	public LabelUpdateSet(PSet<String> posSet, PSet<String> negSet) {
		super(posSet, negSet);
	}

	public static LabelUpdateSet empty() {
		return new LabelUpdateSet();
	}
	@Override
	public PSet<String> plus(String e) {
		return new LabelUpdateSet(getPositiveSet().plus(e), getNegativeSet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public PSet<String> plusAll(Collection<? extends String> list) {
		return new LabelUpdateSet(getPositiveSet().plusAll((Collection<String>) list), getNegativeSet());
	}

	@Override
	public PSet<String> minus(Object e) {
		return new LabelUpdateSet(getPositiveSet(), getNegativeSet().plus((String) e));
	}

	@SuppressWarnings("unchecked")
	@Override
	public PSet<String> minusAll(Collection<?> list) {
		return new LabelUpdateSet(getPositiveSet(), getNegativeSet().plusAll((Collection<String>) list));
	}

	
}
