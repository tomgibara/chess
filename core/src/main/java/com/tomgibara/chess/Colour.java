package com.tomgibara.chess;

public enum Colour {

	WHITE, BLACK;

	public static Colour valueOf(int ordinal) {
		switch (ordinal) {
		case 0 : return WHITE;
		case 1 : return BLACK;
		default: throw new IllegalArgumentException("invalid ordinal: " + ordinal);
		}
	}
	
	public final boolean white;
	public final boolean black;
	public final char character;
	
	private Colour() {
		white = ordinal() == 0;
		black = ordinal() == 1;
		character = white ? 'w' : 'b';
	}
	
	public ColouredPiece colour(Piece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		return ColouredPiece.from(piece, this);
	}
	
	public Colour opposite() {
		return white ? BLACK : WHITE;
	}
	
}