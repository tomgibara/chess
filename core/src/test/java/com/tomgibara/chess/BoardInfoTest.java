package com.tomgibara.chess;

import static com.tomgibara.chess.Colour.BLACK;
import static com.tomgibara.chess.Colour.WHITE;
import static com.tomgibara.chess.Move.move;
import static com.tomgibara.chess.Square.at;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class BoardInfoTest extends TestCase {

	public void testCounts() {
		BoardInfo info = Board.initial().getInfo();
		assertEquals(32, info.countPieces());
		assertEquals(16, info.white().countPieces());
		assertEquals(16, info.black().countPieces());
		assertEquals(16, info.count(Piece.PAWN));
		assertEquals(8, info.count(ColouredPiece.WHITE_PAWN));
		assertEquals(8, info.count(Pieces.containing(Piece.KNIGHT, Piece.BISHOP)));
		assertEquals(
				Squares.immutable(at("e1")),
				info.squaresOccupiedBy(ColouredPiece.WHITE_KING)
				);
		assertEquals(
				Squares.immutable(at("a8"), at("h8")),
				info.squaresOccupiedBy(ColouredPiece.BLACK_ROOK)
				);
		assertTrue(info.black().occupiedSquares().asArea().isRectangular());
		assertEquals(
				Rectangle.from(Rank.RK_1, Rank.RK_2).getSquares(),
				info.white().occupiedSquares()
				);
	}
	
	public void testPins() {
		Board board = Notation.parseFENBoard("7r/1k2pn1R/8/1r1p4/8/3K1B1q/8/1R6");
		Map<Square, Interposition> whitePins = board.getInfo().withColour(WHITE).pinsToKing();
		System.out.println(whitePins);
		Map<Square, Interposition> blackPins = board.getInfo().withColour(BLACK).pinsToKing();
		System.out.println(blackPins);
		List<Move> b5Moves = at("b5").on(board).availableMoves(MoveContraint.defaultBlack);
		System.out.println("B5: " + b5Moves);
		List<Move> f3Moves = at("f3").on(board).availableMoves(MoveContraint.defaultWhite);
		System.out.println("F3: " + f3Moves);
		List<Move> h3Moves = at("h3").on(board).availableMoves(MoveContraint.defaultBlack);
		System.out.println("H3: " + h3Moves);
	}
	
	public void testChecks() {
		Board board = Notation.parseFENBoard("8/3kn2Q/4P3/3K4/B7/8/3r4/3r4");
		SquareMap<Move> whiteChecks = board.getInfo().white().checks();
		System.out.println(whiteChecks);
		SquareMap<Move> blackChecks = board.getInfo().black().checks();
		System.out.println(blackChecks);
	}
	
	public void testKingMoves() {
		Board board = Notation.parseFENBoard("2K5/1B4N1/4k3/4P2Q/8/8/8/8");
		//TODO need a better way to get moves for square
		List<Move> kingMoves = board.getInfo().squaresOccupiedBy(Piece.KING.black()).asArea().on(board).availableMoves(MoveContraint.defaultBlack);
		System.out.println("KING " + kingMoves);
	}
	
	public void testStopCheck() {
		Board board = Notation.parseFENBoard("8/3k2Q1/8/4qn2/8/3K4/8/8");
		List<Move> moves = board.getOccupiedArea().on(board).availableMoves(MoveContraint.defaultBlack);
		System.out.println("CHECK SAVERS " + moves);
	}
	
	public void testEnPassant() {
		Board board = Notation.parseFENBoard("4k3/8/8/8/pP6/8/8/4K3");
		BoardArea area = board.getOccupiedArea().on(board);
		List<Move> moves1 = area.availableMoves(new MoveContraint(Colour.BLACK, false, false, File.FL_B));
		assertTrue(moves1.contains(move("a4-b3")));
		List<Move> moves2 = area.availableMoves(new MoveContraint(Colour.BLACK, false, false, null));
		assertFalse(moves2.contains(move("a4-b3")));
	}
	
	public void testCastling() {
		Board board = Notation.parseFENBoard("4k3/8/8/q7/8/r3b3/3PP3/R3Kb1r");
		BoardArea area = board.getInfo().squaresOccupiedBy(Piece.KING.white()).asArea().on(board);
		List<Move> moves1 = area.availableMoves(new MoveContraint(Colour.WHITE, true, false, null));
		assertTrue(moves1.contains(move("e1-c1")));
		List<Move> moves2 = area.availableMoves(new MoveContraint(Colour.WHITE, false, false, null));
		assertFalse(moves2.contains(move("e1-c1")));
		
		//board = Notation.parseFENBoard("8/8/8/2b5/1rq5/2k5/8/R3K2R");
		board = Notation.parseFENBoard("8/8/8/2b5/1rq5/2k5/5P2/R3K2R");
		area = board.getInfo().squaresOccupiedBy(Piece.KING.white()).asArea().on(board);
		List<Move> moves = area.availableMoves(new MoveContraint(Colour.WHITE, true, true, null));
		assertTrue(moves.contains(move("e1-c1")));
		assertFalse(moves.contains(move("e1-g1")));
	}
}
