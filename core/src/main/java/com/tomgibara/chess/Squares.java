package com.tomgibara.chess;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

//TODO should cache size?
public class Squares extends AbstractSet<Square> {

	private static final Squares empty = new Squares();
	
	private static boolean isSingleBit(long mask) {
		return ((mask != 0) && ((mask & (~mask + 1)) == mask));
	}

	static long mask(Square s) {
		return 1L << s.ordinal;
	}
	
	static long maskObjects(Collection<?> c) {
		if (c instanceof Squares) return ((Squares) c).squares;
		
		long mask = 0L;
		for (Object o : c) {
			if (!(o instanceof Square)) continue;
			mask |= mask((Square) o);
		}
		return mask;
	}
	
	static long maskSquares(Collection<? extends Square> c) {
		if (c instanceof Squares) return ((Squares) c).squares;
		
		long mask = 0L;
		for (Square s : c) {
			if (s == null) throw new IllegalArgumentException("null square");
			mask |= mask(s);
		}
		return mask;
	}

	static long maskSquares(Square... c) {
		if (c == null) throw new IllegalArgumentException("null squares");
		long mask = 0L;
		for (Square s : c) {
			if (s == null) throw new IllegalArgumentException("null square");
			mask |= mask(s);
		}
		return mask;
	}

	public static Squares empty() {
		return empty;
	}
	
	//TODO could cache?
	public static Squares singleton(Square square) {
		if (square == null) throw new IllegalArgumentException("null square");
		return new Squares(1L << square.ordinal);
	}
	
	// avoids copying immutable sets
	public static Squares immutable(Collection<? extends Square> squares) {
		if (squares == null) throw new IllegalArgumentException("null squares");
		return squares.getClass() == Squares.class ? (Squares) squares : new Squares(squares);
	}

	public static Squares immutable(Squares squares) {
		if (squares == null) throw new IllegalArgumentException("null squares");
		return squares.getClass() == Squares.class ? squares : new Squares(squares.squares);
	}

	public static Squares immutable(Square... squares) {
		return new Squares( maskSquares(squares) );
	}

	long squares;

	Squares(long squares) {
		this.squares = squares;
	}
	
	public Squares() {
		this(0L);
	}
	
	public Squares(Collection<? extends Square> c) {
		this(maskSquares(c));
	}
	
	public Area asArea() {
		return Area.squares(this);
	}
	
	@Override
	public int size() {
		return Long.bitCount(squares);
	}
	
	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Square)) return false;
		return (squares & mask((Square) o)) != 0L;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		long mask;
		if (c instanceof Squares) {
			mask = ((Squares) c).squares;
		} else {
			mask = 0L;
			for (Object o : c) {
				if (!(o instanceof Square)) return false;
				mask |= mask((Square) o);
			}
		}
		return (mask & ~squares) == 0L;
	}
	
	@Override
	public Iterator<Square> iterator() {
		return new Iterator<Square>() {
			int ordinal = 0;
			long state = squares;
			
			{
				advance();
			}
			
			@Override
			public boolean hasNext() {
				return state != 0L;
			}
			
			@Override
			public Square next() {
				if (state == 0L) throw new NoSuchElementException();
				Square square = Square.at(ordinal);
				state &= ~1L;
				advance();
				return square;
			}
			
			private void advance() {
				if (state == 0L) return;
				int step = Long.numberOfTrailingZeros(state);
				ordinal += step;
				state >>>= step;
			}
			
		};
	}
	
	@Override
	public boolean isEmpty() {
		return squares == 0L;
	}
	
	@Override
	public void forEach(Consumer<? super Square> action) {
		Objects.requireNonNull(action, "null action");
		long bits = squares;
		for (int ordinal = 0; ordinal < 64 && bits != 0; ordinal++, bits >>>= 1) {
			if ((bits & 1L) == 1L) {
				action.accept(Square.at(ordinal));
			}
		}
	}
	
	boolean isMutable() {
		return getClass() != Squares.class;
	}

	Square first() {
		if (squares == 0L) throw new NoSuchElementException();
		int ordinal = Long.numberOfTrailingZeros(squares);
		return Square.at(ordinal);
	}
	
	Square only() {
		return isSingleBit(squares) ? Square.at(Long.numberOfTrailingZeros(squares)) : null;
	}

	long mask() {
		return squares;
	}
	
	//TODO support publicly as containsAny?
	boolean intersects(Squares that) {
		return (this.squares & that.squares) != 0;
	}
	
	boolean disjoint(Squares that, Square except) {
		long i = this.squares & that.squares;
		return i == 0 || except !=null && i == except.mask;
	}
	
	Squares intersect(Squares that) {
		long mask = this.squares & that.squares;
		if (mask == this.squares) return this;
		if (mask == that.squares) return that;
		if (mask == 0L) return empty;
		return new Squares(mask);
	}
	
	long getMask() {
		return squares;
	}

}
