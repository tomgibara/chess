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
	
	private final ColouredPiece[] pieces;
	private final Area occupiedArea;
	private BoardInfo info = null;
	
	private Board() {
		pieces = new ColouredPiece[64];
		occupiedArea = Area.empty();
	}
	
	Board(Arrangement arrangement) {
		pieces = arrangement.consume();
		occupiedArea = arrangement.occupiedSquares().asArea();
	}
	
	//TODO replace with field access?
	public Area getOccupiedArea() {
		return occupiedArea;
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
		return Arrays.equals(this.pieces, that.pieces);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(pieces);
	}
	
	public String toString() {
		//TODO move to a shared constant when ready
		String nl = String.format("%n");
		StringBuilder sb = new StringBuilder(144);
		for (int rank = 7; rank >= 0; rank--) {
			for (int file = 0; file < 8; file++) {
				ColouredPiece piece = pieceAt(Square.at(file, rank));
				sb.append(piece == null ? "  " : piece.toString());
			}
			sb.append(nl);
		}
		return sb.toString();
	}

	ColouredPiece pieceAt(Square square) {
		return pieces[square.ordinal];
	}
	
	int[] countPieces() {
		int[] counts = new int[12];
		for (ColouredPiece piece : pieces) {
			if (piece == null) continue;
			counts[piece.ordinal()] ++;
		}
		return counts;
	}
	
	Squares[] pieceSquares(int[] pieceCounts) {
		Squares[] pieceSquares = new Squares[ColouredPiece.COUNT];
		for (int i = 0; i < ColouredPiece.COUNT; i++) {
			pieceSquares[i] = new MutableSquares();
		}
		occupiedArea.getSquares().forEach(
				s -> pieceSquares[ pieces[s.ordinal].ordinal() ].add(s)
		);
		return pieceSquares;
	}

	MutableSquares squaresOccupiedBy(Colour colour) {
		MutableSquares squares = new MutableSquares();
		occupiedArea.getSquares().forEach( s -> {
			if (pieces[s.ordinal].colour == colour) squares.add(s);
		});
		return squares;
	}

}
