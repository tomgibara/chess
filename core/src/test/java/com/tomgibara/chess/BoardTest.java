package com.tomgibara.chess;

import static com.tomgibara.chess.Colour.BLACK;
import static com.tomgibara.chess.Colour.WHITE;
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
import static com.tomgibara.chess.Move.move;
import static com.tomgibara.chess.Square.at;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import junit.framework.TestCase;

public class BoardTest extends TestCase {

	public void testInitial() {
		//TODO do with flip
		Board board = Board.empty();
		board = Rank.RK_8.on(board).put(
				BLACK_ROOK,
				BLACK_KNIGHT,
				BLACK_BISHOP,
				BLACK_QUEEN,
				BLACK_KING,
				BLACK_BISHOP,
				BLACK_KNIGHT,
				BLACK_ROOK
				).board;
		board = Rank.RK_7.on(board).fill(Piece.PAWN.black()).board;
		board = Rank.RK_2.on(board).fill(Piece.PAWN.white()).board;
		board = Rank.RK_1.on(board).put(
				WHITE_ROOK,
				WHITE_KNIGHT,
				WHITE_BISHOP,
				WHITE_QUEEN,
				WHITE_KING,
				WHITE_BISHOP,
				WHITE_KNIGHT,
				WHITE_ROOK
				).board;
		System.out.println(board);
		
		assertEquals(Board.initial(), board);
		assertEquals(Board.initial().toString(), board.toString());
	}
	
	public void testFEN() {
		System.out.println(Notation.parseFENBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"));
		System.out.println(Notation.parseFENBoard("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R"));
	}
	
	public void testMoves() {
		Board board = Board.initial();
		List<Move> moves = Rectangle.entire().on(board).availableMoves(MoveConstraint.defaultWhite);
		assertEquals(20, moves.size());
	}

	public void testCounts() {
		Board board = Board.initial();
		assertEquals(32, board.countPieces());
		assertEquals(16, board.white().countPieces());
		assertEquals(16, board.black().countPieces());
		assertEquals(16, board.count(Piece.PAWN));
		assertEquals(8, board.count(ColouredPiece.WHITE_PAWN));
		assertEquals(8, board.count(Pieces.containing(Piece.KNIGHT, Piece.BISHOP)));
		assertEquals(
				Squares.immutable(at("e1")),
				board.squaresOccupiedBy(ColouredPiece.WHITE_KING)
				);
		assertEquals(
				Squares.immutable(at("a8"), at("h8")),
				board.squaresOccupiedBy(ColouredPiece.BLACK_ROOK)
				);
		assertTrue(board.black().occupiedSquares().asArea().isRectangular());
		assertEquals(
				Rectangle.from(Rank.RK_1, Rank.RK_2).getSquares(),
				board.white().occupiedSquares()
				);
	}
	
	public void testPins() {
		Board board = Notation.parseFENBoard("7r/1k2pn1R/8/1r1p4/8/3K1B1q/8/1R6");
		assertInterMovesAre("h3-d3", board.withColour(WHITE).pinsToKing().values());
		assertInterMovesAre("f3-b7,b1-b7", board.withColour(BLACK).pinsToKing().values());
		assertMovesAre("b5-b6,b5-b4,b5-b3,b5-b2,b5-b1", at("b5").on(board).availableMoves(MoveConstraint.defaultBlack));
		assertMovesAre("", at("f3").on(board).availableMoves(MoveConstraint.defaultWhite));
		assertMovesAre("h3-g3,h3-f3,h3-h2,h3-h1,h3-g2,h3-f1,h3-g4,h3-f5,h3-e6,h3-d7,h3-c8,h3-h4,h3-h5,h3-h6,h3-h7", at("h3").on(board).availableMoves(MoveConstraint.defaultBlack));
	}
	
	public void testChecks() {
		Board board = Notation.parseFENBoard("8/3kn2Q/4P3/3K4/B7/8/3r4/3r4");
		assertMovesAre("d2-d5,e7-d5", board.white().checks().values());
		assertMovesAre("e6-d7,a4-d7", board.black().checks().values());
	}
	
	public void testKingMoves() {
		Board board = Notation.parseFENBoard("2K5/1B4N1/4k3/4P2Q/8/8/8/8");
		//TODO need a better way to get moves for square
		List<Move> kingMoves = board.squaresOccupiedBy(Piece.KING.black()).asArea().on(board).availableMoves(MoveConstraint.defaultBlack);
		assertMovesAre("e6-e7", kingMoves);
	}
	
	public void testStopCheck() {
		Board board = Notation.parseFENBoard("8/3k2Q1/8/4qn2/8/3K4/8/8");
		List<Move> moves = board.pieces.keySet().asArea().on(board).availableMoves(MoveConstraint.defaultBlack);
		assertMovesAre("e5-e7,e5-g7,f5-e7,f5-g7,d7-c6,d7-d6,d7-e6,d7-c8,d7-d8,d7-e8", moves);
	}
	
	public void testEnPassant() {
		Board board = Notation.parseFENBoard("4k3/8/8/8/pP6/8/8/4K3");
		BoardArea area = board.pieces.keySet().asArea().on(board);
		List<Move> moves1 = area.availableMoves(new MoveConstraint(Colour.BLACK, false, false, File.FL_B));
		assertTrue(moves1.contains(move("a4-b3")));
		List<Move> moves2 = area.availableMoves(new MoveConstraint(Colour.BLACK, false, false, null));
		assertFalse(moves2.contains(move("a4-b3")));
	}
	
	public void testCastling() {
		Board board = Notation.parseFENBoard("4k3/8/8/q7/8/r3b3/3PP3/R3Kb1r");
		BoardArea area = board.squaresOccupiedBy(Piece.KING.white()).asArea().on(board);
		List<Move> moves1 = area.availableMoves(new MoveConstraint(Colour.WHITE, true, false, null));
		assertTrue(moves1.contains(move("e1-c1")));
		List<Move> moves2 = area.availableMoves(new MoveConstraint(Colour.WHITE, false, false, null));
		assertFalse(moves2.contains(move("e1-c1")));
		
		//board = Notation.parseFENBoard("8/8/8/2b5/1rq5/2k5/8/R3K2R");
		board = Notation.parseFENBoard("8/8/8/2b5/1rq5/2k5/5P2/R3K2R");
		area = board.squaresOccupiedBy(Piece.KING.white()).asArea().on(board);
		List<Move> moves = area.availableMoves(new MoveConstraint(Colour.WHITE, true, true, null));
		assertTrue(moves.contains(move("e1-c1")));
		assertFalse(moves.contains(move("e1-g1")));
	}

	private void assertSquaresAre(String expected, Collection<Square> squares) {
		if (expected.isEmpty()) {
			assertTrue(squares.isEmpty());
		} else {
			Set<Square> set = Arrays.stream(expected.split(",")).map(Square::at).collect(Collectors.toCollection(TreeSet::new));
			assertEquals(set, new TreeSet<>(squares));
		}
	}

	private void assertMovesAre(String expected, Collection<Move> moves) {
		if (expected.isEmpty()) {
			assertTrue(moves.isEmpty());
		} else {
			Set<Move> set = Arrays.stream(expected.split(",")).map(Move::move).collect(Collectors.toCollection(TreeSet::new));
			assertEquals(set, new TreeSet<>(moves));
		}
	}

	private void assertInterMovesAre(String expected, Collection<Interposition> inters) {
		if (expected.isEmpty()) {
			assertTrue(inters.isEmpty());
		} else {
			Set<Move> set = Arrays.stream(expected.split(",")).map(Move::move).collect(Collectors.toCollection(TreeSet::new));
			assertEquals(set, inters.stream().map(i -> i.move).collect(Collectors.toCollection(TreeSet::new)));
		}
	}

}
