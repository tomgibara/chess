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

	public static Colour valueOf(char c) {
		switch (c) {
		case 'w' : return WHITE;
		case 'b' : return BLACK;
		default: throw new IllegalArgumentException("invalid character: " + c);
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
	
	public Piece colour(PieceType type) {
		if (type == null) throw new IllegalArgumentException("null type");
		return Piece.from(type, this);
	}
	
	public Colour opposite() {
		return white ? BLACK : WHITE;
	}
	
}
