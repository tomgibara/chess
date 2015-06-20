package com.tomgibara.chess;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Piece {

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
	
	private static final Piece[] values = values();
	
	public static final int COUNT = 12;
	
	public static final List<Piece> whitePieces = Collections.unmodifiableList(Arrays.asList(
			WHITE_PAWN,
			WHITE_KNIGHT,
			WHITE_BISHOP,
			WHITE_ROOK,
			WHITE_QUEEN,
			WHITE_KING
			));
	
	public static final List<Piece> blackPieces = Collections.unmodifiableList(Arrays.asList(
			BLACK_PAWN,
			BLACK_KNIGHT,
			BLACK_BISHOP,
			BLACK_ROOK,
			BLACK_QUEEN,
			BLACK_KING
			));
	
	public static List<Piece> piecesColoured(Colour colour) {
		if (colour == null) throw new IllegalArgumentException("null colour");
		return colour.white ? whitePieces : blackPieces;
	}
	
	static {
		for (Piece piece : values) {
			int ordinal = piece.ordinal() + ( piece.colour.white ? 1 : -1);
			piece.swapped = values[ordinal];
		}
	}
	
	static Piece from(PieceType type, Colour colour) {
		return values[type.ordinal() * 2 + colour.ordinal()];
	}
	
	public static Piece valueOf(int ordinal) {
		try {
			return values[ordinal];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public final PieceType type;
	public final Colour colour;
	private final String string;
	private Piece swapped;
	
	private Piece() {
		int ordinal = ordinal();
		type = PieceType.valueOf(ordinal >> 1);
		colour = Colour.valueOf(ordinal & 1);
		string = new StringBuilder(2).append(colour.character).append(type.character).toString();
	}
	
	public Piece white() {
		return colour.white ? this : swapped;
	}

	public Piece black() {
		return colour.black ? this : swapped;
	}
	
	public Piece getSwapped() {
		return swapped;
	}
	
	@Override
	public String toString() {
		return string;
	}

}
