package com.tomgibara.chess;

import java.util.Iterator;

final public class Pieces extends SquareMap<Piece> {

	private Pieces(Store<Piece> store) {
		super(store);
	}
	
	public Pieces() {
		super(new Piece[64], 0);
	}
	
	public Pieces(Piece[] pieces) {
		super(pieces);
	}
	
	public Board newBoard() {
		return new Board(this);
	}

	public Pieces set(Square square, Piece piece) {
		if (square == null) throw new IllegalArgumentException("null square");
		if (piece == null) {
			remove(square);
		} else {
			put(square, piece);
		}
		return this;
	}
	
	public Pieces set(Area area, Piece... pieces) {
		if (area == null) throw new IllegalArgumentException("null area");
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		update(area.getSquares(), pieces);
		return this;
	}
	
	public Pieces fill(Area area, Piece piece) {
		if (area == null) throw new IllegalArgumentException("null area");
		area.getSquares().forEach(s -> put(s, piece));
		return this;
	}
	
	public Pieces swapColours() {
		forEach( (s, p) -> put(s, p.getSwapped()));
		return this;
	}
	
	//TODO ugly and messy :(
	@Override
	public Pieces immutable() {
		return (Pieces) super.immutable();
	}
	
	@Override
	public Pieces mutableCopy() {
		return (Pieces) super.mutableCopy();
	}
	
	@Override
	Pieces newInstance(Store<Piece> store) {
		return new Pieces(store);
	}

	private void update(Squares set, Piece... pieces) {
		final int length = pieces.length;
		if (length > 0) {
			int i = 0;
			for (Iterator<Square> it = set.iterator(); i < length && it.hasNext(); i++) {
				Square square = it.next();
				Piece piece = pieces[i];
				if (piece == null) {
					remove(square);
				} else {
					put(square, piece);
				}
			}
		}
	}
	
}
