package de.protubero.beanstore.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

public class UpdatePSet<E> implements PSet<E>, ValueUpdateFunction<PSet<E>>{

	private PSet<E> positiveSet; 
	private PSet<E> negativeSet; 

	private UpdatePSet(PSet<E> positiveSet, PSet<E> negativeSet) {
		this.positiveSet = Objects.requireNonNull(positiveSet);
		this.negativeSet = Objects.requireNonNull(negativeSet);
	}
	
	public static <E> PSet<E> empty() {
		return new UpdatePSet<>(HashTreePSet.empty(), HashTreePSet.empty());
	}
	
	@Override
	public PSet<E> plus(E e) {
		return new UpdatePSet<>(positiveSet.plus(e), negativeSet);
	}

	@Override
	public PSet<E> plusAll(Collection<? extends E> list) {
		return new UpdatePSet<>(positiveSet.plusAll(list), negativeSet);
	}

	@SuppressWarnings("unchecked")
	@Override
	public PSet<E> minus(Object e) {
		return new UpdatePSet<>(positiveSet, negativeSet.plus((E) e));
	}

	@SuppressWarnings("unchecked")
	@Override
	public PSet<E> minusAll(Collection<?> list) {
		return new UpdatePSet<>(positiveSet, negativeSet.plusAll((Collection<? extends E>) list));
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
	public PSet<E> apply(PSet<E> source) {
		for (E e : negativeSet) {
			if (positiveSet.contains(e)) {
				throw new RuntimeException("Same element in pos. and neg. set: " + e);
			}
			source = source.minus(e);
		}
		for (E e : positiveSet) {
			source = source.plus(e);
		}
		return source;
	}


}
