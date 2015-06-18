package com.tomgibara.chess;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

//TODO could move some methods to a specialist to BoardAreaInfo?
//TODO should be a method to check basic validity
public class BoardInfo {

	public final Board board;
	
	private int[] colPieceCounts = null;
	private int pieceCount = -1;
	private Squares[] pieceSquares = null;
	private BoardArea entireBoardArea = null;
	private ColouredBoardInfo whiteInfo;
	private ColouredBoardInfo blackInfo;
	
	BoardInfo(Board board) {
		this.board = board;
	}
	
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
	
	public int count(ColouredPiece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		return getColPieceCounts()[piece.ordinal()];
	}
	
	public int count(Piece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		int[] counts = getColPieceCounts();
		return counts[piece.white().ordinal()] + counts[piece.black().ordinal()];
	}
	
	//makes assumptions
	public int count(Set<Piece> pieces) {
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		if (pieces.isEmpty()) return 0;
		if (pieces.size() == Piece.COUNT) return countPieces();
		
		int[] counts = getColPieceCounts();
		int count = 0;
		for (int ord = 0; ord < Piece.COUNT; ord++) {
			Piece piece = Piece.valueOf(ord);
			if (pieces.contains(piece)) {
				count += counts[piece.white().ordinal()] + counts[piece.black().ordinal()];
			}
		}
		return count;
	}

	public int countPieces() {
		return pieceCount < 0 ? pieceCount = board.pieces.keySet().size() : pieceCount;
	}
	
	// squares
	
	//TODO remove gets
	public BoardArea getEntireBoardArea() {
		return entireBoardArea == null ? entireBoardArea = Area.entire().on(board) : entireBoardArea;
	}

	// convenience method
	public Squares occupiedSquares() {
		return board.pieces.keySet();
	}
	
	public Squares squaresOccupiedBy(ColouredPiece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		return getPieceSquares()[piece.ordinal()];
	}
	
	// check
	
//	//TODO needs memoization
//	public Map<Square, Interposition> pins(ColouredPiece piece) {
//		if (piece == null) throw new IllegalArgumentException("null piece");
//		SquareMap<Interposition> pins = computedPins(piece);
//		return Collections.unmodifiableMap(pins);
//	}
	
//	private SquareMap<Interposition> computedPins(ColouredPiece piece) {
//		Colour opposite = piece.colour.opposite();
//		SquareMap<Interposition> pins = new SquareMap<>(Interposition.class);
//		for (Square square : squaresOccupiedBy(piece)) {
//			analyzePins(square, Piece.BISHOP.coloured(opposite), pins);
//			analyzePins(square, Piece.ROOK.coloured(opposite), pins);
//			analyzePins(square, Piece.QUEEN.coloured(opposite), pins);
//		}
//		return pins;
//	}

//	private SquareMap<Interposition> computePinsOn(Square square, SquareMap<Interposition> pinsFrom, SquareMap<Interposition> pinsThrough) {
//		ColouredPiece piece = board.pieceAt(square);
//		if (piece != null) {
//			Colour opposite = piece.colour.opposite();
//			analyzePins(square, Piece.BISHOP.coloured(opposite), pinsFrom, pinsThrough);
//			analyzePins(square, Piece.ROOK.coloured(opposite), pinsFrom, pinsThrough);
//			analyzePins(square, Piece.QUEEN.coloured(opposite), pinsFrom, pinsThrough);
//		}
//		return pins;
//	}
//
//	public boolean isChecked(Colour colour) {
//		if (colour == null) throw new IllegalArgumentException("null colour");
//		Squares squares = squaresOccupiedBy(Piece.KING.coloured(colour));
//		Square square = squares.only();
//		if (square == null) {
//			//TODO should look for check on any king
//			return false;
//		} else {
//			//move.
//			throw new UnsupportedOperationException();
//		}
//	}
	
	// private utility methods
	
	private int[] getColPieceCounts() {
		return colPieceCounts == null ? colPieceCounts = board.countPieces() : colPieceCounts;
	}
	
	private Squares[] getPieceSquares() {
		if (pieceSquares == null) {
			Squares[] squares = board.pieceSquares(getColPieceCounts());
			for (int i = 0; i < ColouredPiece.COUNT; i++) {
				squares[i] = Squares.immutable(squares[i]);
			}
			pieceSquares = squares;
		}
		return pieceSquares;
	}

	// inner classes
	
	public class ColouredBoardInfo {
		
		public final Colour colour;

		private int pieceCount = -1;
		private Squares occupiedSquares = null;
		private Square kingsSquare = null;
		private SquareMap<Interposition> pinsToKing = null;
		private SquareMap<Interposition> pinnedToKing = null;
		private SquareMap<Move> checks = null;

		
		public ColouredBoardInfo(Colour colour) {
			this.colour = colour;
		}

		//TODO could optimize?
		public Squares occupiedSquares() {
			return occupiedSquares == null ? occupiedSquares = board.squaresOccupiedBy(colour) : occupiedSquares;
		}
		
		public int countPieces() {
			return pieceCount == -1 ? pieceCount = occupiedSquares().size() : pieceCount;
		}
		
		public Square kingsSquare() {
			if (kingsSquare == null) {
				//NOTE may exceptionally remain null
				kingsSquare = getPieceSquares()[Piece.KING.coloured(colour).ordinal()].only();
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
				SquareMap<Move> map = Move.newMoveMap();
				//TODO want to be able to intersect with occupied squares of opposing colour
				// would remove first two if clauses
				SquareMap<ColouredPiece> pieces = board.pieces;
				Move.possibleMovesTo(square).forEach(m -> {
					Square s = m.from;
					ColouredPiece p = pieces.get(s);
					if (
							p != null &&
							p.colour != this.colour &&
							m.isPossibleFor(p) &&
							!m.intermediateSquares.intersects(BoardInfo.this.occupiedSquares())
					) {
						map.put(s, m);
					}
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
		final ColouredPiece targetPiece;
		private final SquareMap<Interposition> pinsFrom = Interposition.newSquareMap();
		private final SquareMap<Interposition> pinsThrough = Interposition.newSquareMap();
		Squares possibleSquares;
		Colour attackColour;
		
		PinAnalyzer(Square square) {
			targetSquare = square;
			targetPiece = board.pieces.get(square);
			if (targetPiece != null) {
				Colour targetColour = targetPiece.colour;
				attackColour = targetColour.opposite();
				possibleSquares = withColour(targetColour).occupiedSquares();
				analyzePins(Piece.BISHOP.coloured(attackColour));
				analyzePins(Piece.ROOK.coloured(attackColour));
				analyzePins(Piece.QUEEN.coloured(attackColour));
			}
		}
		
		SquareMap<Interposition> pinsFrom() {
			return pinsFrom.immutable();
		}

		SquareMap<Interposition> pinsThrough() {
			return pinsThrough.immutable();
		}

		private void analyzePins(ColouredPiece attackPiece ) {
			for (Square square : squaresOccupiedBy(attackPiece)) {
				Move move = Move.between(square, targetSquare);
				if (move.isPossibleFor(attackPiece)) {
					Square only = move.intermediateSquares.intersect(occupiedSquares()).only();
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
