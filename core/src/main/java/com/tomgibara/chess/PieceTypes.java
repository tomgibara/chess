package com.tomgibara.chess;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public class PieceTypes extends AbstractSet<PieceType> {

	private static final PieceTypes[] pieces = new PieceTypes[64];
	
	static {
		for (int i = 0; i < 64; i++) {
			pieces[i] = new PieceTypes(i);
		}
	}
	
	public static PieceTypes containing(
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
		return PieceTypes.pieces[ordinal];
	}
	
	
	public static PieceTypes containing(Collection<PieceType> pieces) {
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		if (pieces instanceof PieceTypes) return (PieceTypes) pieces;
		return containing(
				pieces.contains(PieceType.PAWN),
				pieces.contains(PieceType.KNIGHT),
				pieces.contains(PieceType.BISHOP),
				pieces.contains(PieceType.ROOK),
				pieces.contains(PieceType.QUEEN),
				pieces.contains(PieceType.KING)
				);
	}
	
	public static PieceTypes containing(PieceType... pieces) {
		Set<PieceType> set = EnumSet.noneOf(PieceType.class);
		for (PieceType piece : pieces) {
			set.add(piece);
		}
		return containing(set);
	}
	
	public final int ordinal;
	private final PieceType[] elements;
	
	private PieceTypes(int ordinal) {
		this.ordinal = ordinal;
		int length = Integer.bitCount(ordinal);
		elements = new PieceType[length];
		//TODO create accessor without array copy
		PieceType[] ps = PieceType.values();
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
		if (!(o instanceof PieceType)) return false;
		PieceType p = (PieceType) o;
		return (ordinal & 1 << p.ordinal()) != 0;
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
			return c instanceof PieceTypes ?
					(~ordinal & ((PieceTypes) c).ordinal) == 0 :
						super.containsAll(c);
	}
	
	@Override
	public Iterator<PieceType> iterator() {
		//TODO use direct iterator implementation
		return Arrays.asList(elements).iterator();
	}
	
	public boolean isFull() {
		return ordinal == 63;
	}
	
}
