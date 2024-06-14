package de.protubero.beanstore.linksandlabels;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

public abstract class UpdatePSet<E> implements PSet<E>, ValueUpdateFunction<PSet<E>>{

	private PSet<E> positiveSet; 
	private PSet<E> negativeSet; 

	UpdatePSet(PSet<E> positiveSet, PSet<E> negativeSet) {
		this.positiveSet = Objects.requireNonNull(positiveSet);
		this.negativeSet = Objects.requireNonNull(negativeSet);
	}
		

	@Override
	@Deprecated
	public boolean add(E o) {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean remove(Object o) {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean addAll(Collection<? extends E> c) {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean removeAll(Collection<?> c) {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean retainAll(Collection<?> c) {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void clear() {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public int size() {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean isEmpty() {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean contains(Object o) {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public Iterator<E> iterator() {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public Object[] toArray() {
	    throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public <T> T[] toArray(T[] a) {
	    throw new UnsupportedOperationException();
	}


	@Override
	@Deprecated
	public boolean containsAll(Collection<?> c) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public PSet<E> apply(PSet<E> target) {
		if (target == null) {
			target = HashTreePSet.empty();
		}
		
		for (E e : negativeSet) {
			if (positiveSet.contains(e)) {
				throw new RuntimeException("Same element in pos. and neg. set: " + e);
			}
			target = target.minus(e);
		}
		for (E e : positiveSet) {
			target = target.plus(e);
		}
		return target;
	}

	public PSet<E> getPositiveSet() {
		return positiveSet;
	}

	public PSet<E> getNegativeSet() {
		return negativeSet;
	}


}
