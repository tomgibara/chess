package com.tomgibara.chess;

import static com.tomgibara.chess.ColouredPiece.BLACK_BISHOP;
import static com.tomgibara.chess.ColouredPiece.BLACK_KING;
import static com.tomgibara.chess.ColouredPiece.BLACK_KNIGHT;
import static com.tomgibara.chess.ColouredPiece.BLACK_QUEEN;
import static com.tomgibara.chess.ColouredPiece.BLACK_ROOK;
import static com.tomgibara.chess.ColouredPiece.WHITE_BISHOP;
import static com.tomgibara.chess.ColouredPiece.WHITE_KING;
import static com.tomgibara.chess.ColouredPiece.WHITE_KNIGHT;
import static com.tomgibara.chess.ColouredPiece.WHITE_QUEEN;
import static com.tomgibara.chess.ColouredPiece.WHITE_ROOK;

import java.util.Arrays;
import java.util.function.BiConsumer;

//TODO what role does this play now - move Board info functionality here?
public final class Board {

	private static final Board empty = new Board();
	private static final Board initial = new Board(
			new Arrangement()
			.set(Rank.RK_8.asRectangle().getSquares(),
					BLACK_ROOK,
					BLACK_KNIGHT,
					BLACK_BISHOP,
					BLACK_QUEEN,
					BLACK_KING,
					BLACK_BISHOP,
					BLACK_KNIGHT,
					BLACK_ROOK
					)
			.fill(Rank.RK_7.asRectangle().getSquares(), ColouredPiece.BLACK_PAWN)
			.fill(Rank.RK_2.asRectangle().getSquares(), ColouredPiece.WHITE_PAWN)
			.set(Rank.RK_1.asRectangle().getSquares(),
					WHITE_ROOK,
					WHITE_KNIGHT,
					WHITE_BISHOP,
					WHITE_QUEEN,
					WHITE_KING,
					WHITE_BISHOP,
					WHITE_KNIGHT,
					WHITE_ROOK
					)
			);
	
	public static Board empty() {
		return empty;
	}
	
	public static Board initial() {
		return initial;
	}

	public final SquareMap<ColouredPiece> pieces;
	private BoardInfo info = null;
	
	private Board() {
		pieces = new Arrangement().consume();
	}

	//TODO quick test only
	Board(SquareMap<ColouredPiece> pieces) {
		this.pieces = pieces;
	}

	Board(Arrangement arrangement) {
		pieces = arrangement.consume();
	}
	
	public BoardInfo getInfo() {
		return info == null ? info = new BoardInfo(this) : info;
	}
	
	public Arrangement newArrangement() {
		return new Arrangement(pieces);
	}
	
	public BoardArea area(Area area) {
		if (area == null) throw new IllegalArgumentException("null area");
		return new BoardArea(this, area);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Board)) return false;
		Board that = (Board) obj;
		return this.pieces.equals( that.pieces );
	}
	
	@Override
	public int hashCode() {
		return pieces.hashCode();
	}
	
	public String toString() {
		//TODO move to a shared constant when ready
		String nl = String.format("%n");
		StringBuilder sb = new StringBuilder(144);
		for (int rank = 7; rank >= 0; rank--) {
			sb.append(Rank.valueOf(rank)).append(' ');
			for (int file = 0; file < 8; file++) {
				ColouredPiece piece = pieces.get(Square.at(file, rank));
				sb.append(piece == null ? "  " : piece.toString());
			}
			sb.append(nl);
		}
		sb.append(' ');
		for (File file: File.values()) {
			sb.append(' ').append(file.character);
		}
		sb.append(' ');
		sb.append(nl);
		return sb.toString();
	}

}
