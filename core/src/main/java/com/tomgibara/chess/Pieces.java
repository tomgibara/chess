package com.tomgibara.chess;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public class Pieces extends AbstractSet<Piece> {

	private static final Pieces[] pieces = new Pieces[64];
	
	static {
		for (int i = 0; i < 64; i++) {
			pieces[i] = new Pieces(i);
		}
	}
	
	public static Pieces containing(
			boolean pawn,
			boolean knight,
			boolean bishop,
			boolean rook,
			boolean queen,
			boolean king
			) {
		int ordinal = 0;
		if (pawn  ) ordinal +=  1;
		if (knight) ordinal +=  2;
		if (bishop) ordinal +=  4;
		if (rook  ) ordinal +=  8;
		if (queen ) ordinal += 16;
		if (king  ) ordinal += 32;
		return Pieces.pieces[ordinal];
	}
	
	
	public static Pieces containing(Collection<Piece> pieces) {
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		if (pieces instanceof Pieces) return (Pieces) pieces;
		return containing(
				pieces.contains(Piece.PAWN),
				pieces.contains(Piece.KNIGHT),
				pieces.contains(Piece.BISHOP),
				pieces.contains(Piece.ROOK),
				pieces.contains(Piece.QUEEN),
				pieces.contains(Piece.KING)
				);
	}
	
	public static Pieces containing(Piece... pieces) {
		Set<Piece> set = EnumSet.noneOf(Piece.class);
		for (Piece piece : pieces) {
			set.add(piece);
		}
		return containing(set);
	}
	
	public final int ordinal;
	private final Piece[] elements;
	
	private Pieces(int ordinal) {
		this.ordinal = ordinal;
		int length = Integer.bitCount(ordinal);
		elements = new Piece[length];
		//TODO create accessor without array copy
		Piece[] ps = Piece.values();
		int e = 0;
		for (int i = 0; i < ps.length; i++) {
			if ((ordinal & 1 << i) != 0) {
				elements[e++] = ps[i];
			}
		}
	}
	
	@Override
	public int size() {
		return elements.length;
	}
	
	@Override
	public boolean isEmpty() {
		return ordinal == 0;
	}
	
	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Piece)) return false;
		Piece p = (Piece) o;
		return (ordinal & 1 << p.ordinal()) != 0;
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
			return c instanceof Pieces ?
					(~ordinal & ((Pieces) c).ordinal) == 0 :
						super.containsAll(c);
	}
	
	@Override
	public Iterator<Piece> iterator() {
		//TODO use direct iterator implementation
		return Arrays.asList(elements).iterator();
	}
	
	public boolean isFull() {
		return ordinal == 63;
	}
	
}
