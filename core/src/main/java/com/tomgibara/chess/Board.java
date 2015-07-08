package com.tomgibara.chess;

import static com.tomgibara.chess.Piece.BLACK_BISHOP;
import static com.tomgibara.chess.Piece.BLACK_KING;
import static com.tomgibara.chess.Piece.BLACK_KNIGHT;
import static com.tomgibara.chess.Piece.BLACK_QUEEN;
import static com.tomgibara.chess.Piece.BLACK_ROOK;
import static com.tomgibara.chess.Piece.WHITE_BISHOP;
import static com.tomgibara.chess.Piece.WHITE_KING;
import static com.tomgibara.chess.Piece.WHITE_KNIGHT;
import static com.tomgibara.chess.Piece.WHITE_QUEEN;
import static com.tomgibara.chess.Piece.WHITE_ROOK;

import java.util.Set;

public final class Board {

	private static final Board empty = new Pieces().newBoard();
	private static final Board initial = new Pieces()
			.set(Rank.RK_8.asArea(),
					BLACK_ROOK,
					BLACK_KNIGHT,
					BLACK_BISHOP,
					BLACK_QUEEN,
					BLACK_KING,
					BLACK_BISHOP,
					BLACK_KNIGHT,
					BLACK_ROOK
					)
			.fill(Rank.RK_7.asArea(), Piece.BLACK_PAWN)
			.fill(Rank.RK_2.asArea(), Piece.WHITE_PAWN)
			.set(Rank.RK_1.asArea(),
					WHITE_ROOK,
					WHITE_KNIGHT,
					WHITE_BISHOP,
					WHITE_QUEEN,
					WHITE_KING,
					WHITE_BISHOP,
					WHITE_KNIGHT,
					WHITE_ROOK
					).newBoard();
	
	public static Board empty() {
		return empty;
	}
	
	public static Board initial() {
		return initial;
	}

	public final Pieces pieces;
	
	private int[] colPieceCounts = null;
	private int pieceCount = -1;
	private Squares[] pieceSquares = null;
	private ColouredBoardInfo whiteInfo;
	private ColouredBoardInfo blackInfo;
	private Squares[] colourOccupiedSquares = null;
	
	Board(Pieces pieces) {
		this.pieces = pieces;
	}
	
	// info methods
	
	public ColouredBoardInfo withColour(Colour colour) {
		if (colour == null) throw new IllegalArgumentException("null colour");
		return colour.white ? white() : black();
	}
	
	public ColouredBoardInfo white() {
		return whiteInfo == null ? whiteInfo = new ColouredBoardInfo(Colour.WHITE) : whiteInfo;
	}
	
	public ColouredBoardInfo black() {
		return blackInfo == null ? blackInfo = new ColouredBoardInfo(Colour.BLACK) : blackInfo;
	}
	
	// counting
	
	public int count(Piece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		return getColPieceCounts()[piece.ordinal()];
	}
	
	public int count(PieceType type) {
		if (type == null) throw new IllegalArgumentException("null type");
		int[] counts = getColPieceCounts();
		return counts[type.white().ordinal()] + counts[type.black().ordinal()];
	}
	
	//makes assumptions
	public int count(Set<PieceType> types) {
		if (types == null) throw new IllegalArgumentException("null types");
		if (types.isEmpty()) return 0;
		if (types.size() == PieceType.COUNT) return countPieces();
		
		int[] counts = getColPieceCounts();
		int count = 0;
		for (int ord = 0; ord < PieceType.COUNT; ord++) {
			PieceType type = PieceType.valueOf(ord);
			if (types.contains(type)) {
				count += counts[type.white().ordinal()] + counts[type.black().ordinal()];
			}
		}
		return count;
	}

	public int countPieces() {
		return pieceCount < 0 ? pieceCount = pieces.keySet().size() : pieceCount;
	}
	
	// squares
	
	public Squares squaresOccupiedBy(Piece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		return getPieceSquares()[piece.ordinal()];
	}

	// object methods
	
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

	@Override
	public String toString() {
		//TODO move to a shared constant when ready
		String nl = String.format("%n");
		StringBuilder sb = new StringBuilder(144);
		for (int rank = 7; rank >= 0; rank--) {
			sb.append(Rank.valueOf(rank)).append(' ');
			for (int file = 0; file < 8; file++) {
				Piece piece = pieces.get(Square.at(file, rank));
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
	
	// private utility methods

	private Squares[] colourOccupiedSquares() {
		return colourOccupiedSquares == null ? colourOccupiedSquares = pieces.colourPartition() : colourOccupiedSquares;
	}
	

	private int[] getColPieceCounts() {
		return colPieceCounts == null ? colPieceCounts = pieces.partitionSizes() : colPieceCounts;
	}
	
	//TODO consider switching to Squares array as board representation
	private Squares[] getPieceSquares() {
		return pieceSquares == null ? pieceSquares = pieces.partition() : pieceSquares;
	}

	// inner classes
	
	public class ColouredBoardInfo {
		
		public final Colour colour;

		private int pieceCount = -1;
		private Square kingsSquare = null;
		private SquareMap<Interposition> pinsToKing = null;
		private SquareMap<Interposition> pinnedToKing = null;
		private SquareMap<Move> checks = null;

		
		public ColouredBoardInfo(Colour colour) {
			this.colour = colour;
		}
		
		public Squares occupiedSquares() {
			return colourOccupiedSquares()[colour.ordinal()];
		}

		public int countPieces() {
			return pieceCount == -1 ? pieceCount = occupiedSquares().size() : pieceCount;
		}
		
		public Square kingsSquare() {
			if (kingsSquare == null) {
				//NOTE may exceptionally remain null
				kingsSquare = getPieceSquares()[PieceType.KING.coloured(colour).ordinal()].only();
			}
			return kingsSquare;
		}
		
		public SquareMap<Interposition> pinsToKing() {
			if (pinsToKing == null) computePins();
			return pinsToKing;
		}
		
		public SquareMap<Interposition> pinnedToKing() {
			if (pinnedToKing == null) computePins();
			return pinnedToKing;
		}
		
		public SquareMap<Move> checks() {
			Square square = kingsSquare();
			if (square == null) {
				checks = Move.emptyMap();
			} else {
				SquareMap<Move> map = Move.newSquareMap();
				//TODO want to be able to intersect with occupied squares of opposing colour
				// would remove first two if clauses
				Move.possibleMovesTo(square).forEach(m -> {
					Square s = m.from;
					Piece p = pieces.get(s);
					if (p == null || p.colour == this.colour || !m.isPossibleFor(p)) return;
					switch (p.type) {
					case PAWN:
						if (!m.isPawnCapture()) return;
						break;
					case KING:
						if (m.isCastling()) return;
						break;
					default:
						if (m.intermediateSquares.intersects(pieces.keySet())) return;
					}
					map.put(s, m);
				});
				checks = map.immutable();
			}
			return checks;
		}
		
		private void computePins() {
			Square square = kingsSquare();
			if (square == null) {
				pinsToKing = Interposition.emptySquareMap();
				pinnedToKing = Interposition.emptySquareMap();
			} else {
				PinAnalyzer analyzer = new PinAnalyzer(square);
				pinsToKing = analyzer.pinsFrom();
				pinnedToKing = analyzer.pinsThrough();
			}
		}

	}
	
	private class PinAnalyzer {
		
		final Square targetSquare;
		final Piece targetPiece;
		private final SquareMap<Interposition> pinsFrom = Interposition.newSquareMap();
		private final SquareMap<Interposition> pinsThrough = Interposition.newSquareMap();
		Squares possibleSquares;
		Colour attackColour;
		
		PinAnalyzer(Square square) {
			targetSquare = square;
			targetPiece = pieces.get(square);
			if (targetPiece != null) {
				Colour targetColour = targetPiece.colour;
				attackColour = targetColour.opposite();
				possibleSquares = withColour(targetColour).occupiedSquares();
				analyzePins(PieceType.BISHOP.coloured(attackColour));
				analyzePins(PieceType.ROOK.coloured(attackColour));
				analyzePins(PieceType.QUEEN.coloured(attackColour));
			}
		}
		
		SquareMap<Interposition> pinsFrom() {
			return pinsFrom.immutable();
		}

		SquareMap<Interposition> pinsThrough() {
			return pinsThrough.immutable();
		}

		private void analyzePins(Piece attackPiece ) {
			for (Square square : squaresOccupiedBy(attackPiece)) {
				Move move = Move.between(square, targetSquare);
				if (move.isPossibleFor(attackPiece)) {
					Square only = move.intermediateSquares.intersect(pieces.keySet()).only();
					if (only != null && possibleSquares.contains(only)) {
						Interposition inter = new Interposition(move, only);
						pinsFrom.put(move.from, inter);
						pinsThrough.put(only, inter);
					}
				}
			}
		}

	}
	
}
