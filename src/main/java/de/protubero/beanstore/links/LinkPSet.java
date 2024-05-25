package de.protubero.beanstore.links;

import java.util.Collection;
import java.util.Iterator;

import org.pcollections.PSet;

public class LinkPSet implements PSet<Link<?, ?>> {

	
	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<Link<?, ?>> iterator() {
		return null;
	}

	@Override
	public Object[] toArray() {
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(Link<?, ?> e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Link<?, ?>> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkPSet plus(Link<?, ?> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkPSet plusAll(Collection<? extends Link<?, ?>> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkPSet minus(Object e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkPSet minusAll(Collection<?> list) {
		return null;
	}

	public static LinkPSet empty() {
		return null;
	}

}
