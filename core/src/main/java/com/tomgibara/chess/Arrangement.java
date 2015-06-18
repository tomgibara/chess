package com.tomgibara.chess;

import java.util.Iterator;

//TODO implement object methods
//TODO base on SquareMap?
public class Arrangement {

	private final ColouredPiece[] pieces = new ColouredPiece[64];
	private boolean consumed = false;
	
	public Arrangement() {
	}
	
	public Arrangement(ColouredPiece[] pieces) {
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		if (pieces.length != 64) throw new IllegalArgumentException("invalid pieces");
		System.arraycopy(pieces, 0, this.pieces, 0, 64);
	}

	public Arrangement set(Square square, ColouredPiece piece) {
		if (square == null) throw new IllegalArgumentException("null square");
		checkNotConsumed();
		pieces[square.ordinal] = piece;
		return this;
	}
	
	public Arrangement set(Squares set, ColouredPiece... pieces) {
		if (set == null) throw new IllegalArgumentException("null set");
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		final int length = pieces.length;
		if (length > 0) {
			int i = 0;
			for (Iterator<Square> it = set.iterator(); i < length && it.hasNext(); i++) {
				this.pieces[ it.next().ordinal ] = pieces[i];
			}
		}
		return this;
	}
	
	public Arrangement fill(Squares set, ColouredPiece piece) {
		if (set == null) throw new IllegalArgumentException("null set");
		checkNotConsumed();
		set.forEach(p -> pieces[p.ordinal] = piece);
		return this;
	}
	
	public ColouredPiece get(Square square) {
		if (square == null) throw new IllegalArgumentException("null square");
		checkNotConsumed();
		return pieces[square.ordinal];
	}
	
	public Arrangement swap() {
		checkNotConsumed();
		for (int i = 0; i < pieces.length; i++) {
			ColouredPiece piece = pieces[i];
			if (piece != null) pieces[i] = piece.getSwapped();
		}
		return this;
	}
	
	public Squares occupiedSquares() {
		long squares = 0L;
		for (int i = 0; i < 64; i++) {
			if (pieces[i] != null) squares |= 1L << i;
		}
		return new Squares(squares);
	}

	public Board toBoard() {
		return new Board(this);
	}
	
	ColouredPiece[] consume() {
		checkNotConsumed();
		return pieces;
	}
	
	private void checkNotConsumed() {
		if (consumed) throw new IllegalStateException("consumed");
	}
	
}
