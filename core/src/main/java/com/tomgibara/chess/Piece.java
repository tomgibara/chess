package com.tomgibara.chess;

public enum Piece {

	PAWN('P'),
	KNIGHT('N'),
	BISHOP('B'),
	ROOK('R'),
	QUEEN('Q'),
	KING('K');

	private static final Piece[] values = values();
	
	public static Piece valueOf(int ordinal) {
		try {
			return values[ordinal];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static final int COUNT = 6;
	
	//TODO could generate lookup array
	public static Piece valueOf(char c) {
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
	
	private Piece(char c) {
		character = c;
	}
	
	public ColouredPiece white() {
		return ColouredPiece.from(this, Colour.WHITE);
	}
	
	public ColouredPiece black() {
		return ColouredPiece.from(this, Colour.BLACK);
	}
	
	public ColouredPiece coloured(Colour colour) {
		return ColouredPiece.from(this, colour);
	}
	
}
