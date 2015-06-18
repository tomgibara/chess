package com.tomgibara.chess;

import java.util.Arrays;
import java.util.Iterator;

//TODO implement object methods
//TODO replace with square map?
public class Arrangement {

	private final SquareMap<ColouredPiece> pieces;
	private boolean consumed = false;
	
	public Arrangement() {
		pieces = new SquareMap<>(new ColouredPiece[64]);
	}
	
	public Arrangement(ColouredPiece[] pieces) {
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		if (pieces.length != 64) throw new IllegalArgumentException("invalid pieces");
		this.pieces = new SquareMap<>(pieces.clone());
	}

	public Arrangement(SquareMap<ColouredPiece> pieces) {
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		this.pieces = pieces.mutableCopy();
	}

	
	public Arrangement set(Square square, ColouredPiece piece) {
		if (square == null) throw new IllegalArgumentException("null square");
		checkNotConsumed();
		pieces.put(square, piece);
		return this;
	}
	
	public Arrangement set(Squares set, ColouredPiece... pieces) {
		if (set == null) throw new IllegalArgumentException("null set");
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		final int length = pieces.length;
		if (length > 0) {
			int i = 0;
			for (Iterator<Square> it = set.iterator(); i < length && it.hasNext(); i++) {
				Square square = it.next();
				ColouredPiece piece = pieces[i];
				if (piece == null) {
					this.pieces.remove(square);
				} else {
					this.pieces.put(square, piece);
				}
			}
		}
		return this;
	}
	
	public Arrangement fill(Squares set, ColouredPiece piece) {
		if (set == null) throw new IllegalArgumentException("null set");
		checkNotConsumed();
		set.forEach(s -> pieces.put(s, piece));
		return this;
	}
	
	public ColouredPiece get(Square square) {
		if (square == null) throw new IllegalArgumentException("null square");
		checkNotConsumed();
		return pieces.get(square);
	}
	
	public Arrangement swap() {
		checkNotConsumed();
		pieces.forEach( (s, p) -> pieces.put(s, p.getSwapped()));
		return this;
	}
	
	public Squares occupiedSquares() {
		return pieces.keySet();
	}

	public Board toBoard() {
		return new Board(this);
	}
	
	SquareMap<ColouredPiece> consume() {
		checkNotConsumed();
		return pieces.immutable();
	}
	
	private void checkNotConsumed() {
		if (consumed) throw new IllegalStateException("consumed");
	}
	
}
