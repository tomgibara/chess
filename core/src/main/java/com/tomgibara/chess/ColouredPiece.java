package com.tomgibara.chess;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ColouredPiece {

	WHITE_PAWN(),
	BLACK_PAWN(),
	WHITE_KNIGHT(),
	BLACK_KNIGHT(),
	WHITE_BISHOP(),
	BLACK_BISHOP(),
	WHITE_ROOK(),
	BLACK_ROOK(),
	WHITE_QUEEN(),
	BLACK_QUEEN(),
	WHITE_KING(),
	BLACK_KING();
	
	private static final ColouredPiece[] values = values();
	
	public static final int COUNT = 12;
	
	public static final List<ColouredPiece> whitePieces = Collections.unmodifiableList(Arrays.asList(
			WHITE_PAWN,
			WHITE_KNIGHT,
			WHITE_BISHOP,
			WHITE_ROOK,
			WHITE_QUEEN,
			WHITE_KING
			));
	
	public static final List<ColouredPiece> blackPieces = Collections.unmodifiableList(Arrays.asList(
			BLACK_PAWN,
			BLACK_KNIGHT,
			BLACK_BISHOP,
			BLACK_ROOK,
			BLACK_QUEEN,
			BLACK_KING
			));
	
	public static List<ColouredPiece> piecesColoured(Colour colour) {
		if (colour == null) throw new IllegalArgumentException("null colour");
		return colour.white ? whitePieces : blackPieces;
	}
	
	static {
		for (ColouredPiece piece : values) {
			int ordinal = piece.ordinal() + ( piece.colour.white ? 1 : -1);
			piece.swapped = values[ordinal];
		}
	}
	
	static ColouredPiece from(Piece piece, Colour colour) {
		return values[piece.ordinal() * 2 + colour.ordinal()];
	}
	
	public static ColouredPiece valueOf(int ordinal) {
		try {
			return values[ordinal];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public final Piece piece;
	public final Colour colour;
	private final String string;
	private ColouredPiece swapped;
	
	private ColouredPiece() {
		int ordinal = ordinal();
		piece = Piece.valueOf(ordinal >> 1);
		colour = Colour.valueOf(ordinal & 1);
		string = new StringBuilder(2).append(colour.character).append(piece.character).toString();
	}
	
	public ColouredPiece white() {
		return colour.white ? this : swapped;
	}

	public ColouredPiece black() {
		return colour.black ? this : swapped;
	}
	
	public ColouredPiece getSwapped() {
		return swapped;
	}
	
	@Override
	public String toString() {
		return string;
	}

}
