package com.tomgibara.chess;

import java.util.Collection;

//TODO could implement sorted set
class MutableSquares extends Squares {

	MutableSquares() { }
	
	MutableSquares(long squares) {
		super(squares);
	}
	
	MutableSquares(Collection<Square> other) {
		super(other);
	}
	
	@Override
	public boolean add(Square e) {
		long before = squares;
		squares |= mask(e);
		return squares != before;
	}
	
	@Override
	public boolean remove(Object o) {
		if (!(o instanceof Square)) return false;
		long before = squares;
		squares &= ~mask((Square) o);
		return squares != before;
	}
	
	@Override
	public boolean addAll(Collection<? extends Square> c) {
		long before = squares;
		squares |= maskSquares(c);
		return squares != before;
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		long before = squares;
		squares &= maskObjects(c);
		return squares != before;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		long before = squares;
		squares &= ~maskObjects(c);
		return squares != before;
	}
	
	@Override
	public void clear() {
		squares = 0L;
	}
	
	void add(int ordinal) {
		squares |= 1L << ordinal;
	}
	
	void remove(int ordinal) {
		squares &= ~(1L << ordinal);
	}

}
