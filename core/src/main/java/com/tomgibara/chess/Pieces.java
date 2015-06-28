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
		//public callers can only make immutable boards
		return new Board(this.immutable());
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
	
	void make(Colour colour, Move move, MovePieces pieces) {
		PieceType moved = pieces.moved;
		remove(move.from);
		switch (moved) {
		case KING:
			if (move.isCastling()) {
				Move rookMove = move.inducedRookMove();
				remove(rookMove.from);
				put(rookMove.to, PieceType.ROOK.coloured(colour));
			}
			break;
		case PAWN:
			// deal with en-passant
			if (move.isPawnCapture() && pieces.captured == null) {
				remove(move.enPassantSquare());
			}
			if (move.isPromotion()) {
				put(move.to, pieces.promotion.coloured(colour));
				return;
			}
			break;
			default: /* fall through */
		}
		put(move.to, moved.coloured(colour));
	}
	
	void takeBack(Colour colour, Move move, MovePieces pieces) {
		PieceType moved = pieces.moved;
		put(move.from, moved.coloured(colour));
		if (pieces.captured == null) {
			if (moved == PieceType.PAWN && move.isPawnCapture()) {
				put(move.enPassantSquare(), PieceType.PAWN.coloured(colour.opposite()));
			}
			remove(move.to);
		} else {
			put(move.to, pieces.captured.coloured(colour.opposite()) );
		}
		if (moved == PieceType.KING && move.isCastling()) {
			Move rookMove = move.inducedRookMove();
			remove(rookMove.to);
			put(rookMove.from, PieceType.ROOK.coloured(colour));
		}
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
