package com.tomgibara.chess;

public enum PieceType {

	PAWN('P', false),
	KNIGHT('N', true),
	BISHOP('B', true),
	ROOK('R', true),
	QUEEN('Q', true),
	KING('K', false);

	private static final PieceType[] values = values();
	
	public static PieceType valueOf(int ordinal) {
		try {
			return values[ordinal];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static final int COUNT = 6;
	
	//TODO could generate lookup array
	public static PieceType valueOf(char c) {
		switch (c) {
		case 'P' : return PAWN;
		case 'N' : return KNIGHT;
		case 'B' : return BISHOP;
		case 'R' : return ROOK;
		case 'Q' : return QUEEN;
		case 'K' : return KING;
		default: throw new IllegalArgumentException("Unknown piece character: " + c);
		}
	}
	
	public final char character;
	public final boolean promotion;
	
	private PieceType(char c, boolean p) {
		character = c;
		promotion = p;
	}
	
	public Piece white() {
		return Piece.from(this, Colour.WHITE);
	}
	
	public Piece black() {
		return Piece.from(this, Colour.BLACK);
	}
	
	public Piece coloured(Colour colour) {
		return Piece.from(this, colour);
	}
	
}
