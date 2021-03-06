package com.tomgibara.chess;

import static com.tomgibara.chess.CastlingRights.______WG_WC;
import static com.tomgibara.chess.Colour.BLACK;
import static com.tomgibara.chess.Colour.WHITE;
import static com.tomgibara.chess.Move.move;
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
import static com.tomgibara.chess.Square.at;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import junit.framework.TestCase;

public class BoardTest extends TestCase {

	public void testInitial() {
		//TODO do with flip
		Board board = new Pieces()
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
				.fill(Rank.RK_7.asArea() , PieceType.PAWN.black() )
				.fill(Rank.RK_2.asArea(), PieceType.PAWN.white() )
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
		
		assertEquals(Board.initial(), board);
		assertEquals(Board.initial().toString(), board.toString());
	}
	
	public void testFEN() {
		System.out.println(Notation.parseFENPieces("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR").newBoard());
		System.out.println(Notation.parseFENPieces("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R").newBoard());
	}
	
	public void testMoves() {
		Board board = Board.initial();
		List<Move> moves = board.pieces.newPositionFor(Colour.WHITE).moves().moveList();
		assertEquals(20, moves.size());
	}

	public void testCounts() {
		Board board = Board.initial();
		assertEquals(32, board.countPieces());
		assertEquals(16, board.white().countPieces());
		assertEquals(16, board.black().countPieces());
		assertEquals(16, board.count(PieceType.PAWN));
		assertEquals(8, board.count(Piece.WHITE_PAWN));
		assertEquals(8, board.count(PieceTypes.containing(PieceType.KNIGHT, PieceType.BISHOP)));
		assertEquals(
				Squares.immutable(at("e1")),
				board.squaresOccupiedBy(Piece.WHITE_KING)
				);
		assertEquals(
				Squares.immutable(at("a8"), at("h8")),
				board.squaresOccupiedBy(Piece.BLACK_ROOK)
				);
		assertTrue(board.black().occupiedSquares().asArea().isRectangular());
		assertEquals(
				Rectangle.from(Rank.RK_1, Rank.RK_2).getSquares(),
				board.white().occupiedSquares()
				);
	}
	
	public void testPins() {
		Pieces pieces = Notation.parseFENPieces("7r/1k2pn1R/8/1r1p4/8/3K1B1q/8/1R6");
		assertInterMovesAre("h3-d3", pieces.newBoard().withColour(WHITE).pinsToKing().values());
		assertInterMovesAre("f3-b7,b1-b7", pieces.newBoard().withColour(BLACK).pinsToKing().values());
		assertMovesAre("b5-b6,b5-b4,b5-b3,b5-b2,b5-b1", pieces.newPositionFor(BLACK).computeMoves(at("b5").asArea()).moveList());
		assertMovesAre("", pieces.newPositionFor(WHITE).computeMoves(at("f3").asArea()).moveList());
		assertMovesAre("h3-g3,h3-f3,h3-h2,h3-h1,h3-g2,h3-f1,h3-g4,h3-f5,h3-e6,h3-d7,h3-c8,h3-h4,h3-h5,h3-h6,h3-h7", pieces.newPositionFor(BLACK).computeMoves(at("h3").asArea()).moveList());
	}
	
	public void testChecks() {
		Board board = Notation.parseFENPieces("8/3kn2Q/4P3/3K4/B7/8/3r4/3r4").newBoard();
		assertMovesAre("d2-d5,e7-d5", board.white().checks().values());
		assertMovesAre("e6-d7,a4-d7", board.black().checks().values());
	}
	
	public void testKingMoves() {
		Board board = Notation.parseFENPieces("2K5/1B4N1/4k3/4P2Q/8/8/8/8").newBoard();
		//TODO need a better way to get moves for square
		Area kingsArea = board.squaresOccupiedBy(PieceType.KING.black()).asArea();
		List<Move> kingMoves = board.pieces.newPositionFor(BLACK).computeMoves(kingsArea).moveList();
		assertMovesAre("e6-e7", kingMoves);
	}
	
	public void testStopCheck() {
		Pieces pieces = Notation.parseFENPieces("8/3k2Q1/8/4qn2/8/3K4/8/8");
		List<Move> moves = pieces.newPositionFor(BLACK).computeMoves(pieces.keySet().asArea()).moveList();
		assertMovesAre("e5-e7,e5-g7,f5-e7,f5-g7,d7-c6,d7-d6,d7-e6,d7-c8,d7-d8,d7-e8", moves);
	}
	
	public void testEnPassant() {
		Pieces pieces = Notation.parseFENPieces("4k3/8/8/8/pP6/8/8/4K3");
		Area area = pieces.keySet().asArea();
		List<Move> moves1 = pieces.newPositionFor(BLACK, ______WG_WC, File.FL_B).computeMoves(area).moveList();
		assertTrue(moves1.contains(move("a4-b3")));
		List<Move> moves2 = pieces.newPositionFor(BLACK, ______WG_WC, null).computeMoves(area).moveList();
		assertFalse(moves2.contains(move("a4-b3")));
	}
	
	public void testEnPassantCheckCapture() {
		Position position = Notation.parseFENPosition("8/8/7p/5ppP/7K/8/7k/8 w - g6 0 1");
		assertMovesAre("h5-g6", position.moves().moveList());
	}
	
	public void testEnPassantCheckInterpose() {
		Position safe = Notation.parseFENPosition("7k/8/5q1K/6pP/8/8/8/8 w - g6 0 1");
		assertMovesAre("h5-g6", safe.moves().moveList());
		Position mate = Notation.parseFENPosition("7k/8/5q1K/6pP/8/8/8/8 w - - 0 1");
		assertMovesAre("", mate.moves().moveList());
		Position mate2 = Notation.parseFENPosition("7k/8/5q1K/6pP/7r/8/8/8 w - g6 0 1");
		assertMovesAre("", mate2.moves().moveList());
	}
	
	public void testPinnedMate() {
		Position mate = Notation.parseFENPosition("8/7q/8/4r1NK/8/7k/8/8 w - - 0 1");
		assertMovesAre("", mate.moves().moveList());
		
	}
	
	public void testCastling() {
		Pieces pieces = Notation.parseFENPieces("4k3/8/8/q7/8/r3b3/3PP3/R3Kb1r");
		Area area = pieces.newBoard().squaresOccupiedBy(PieceType.KING.white()).asArea();
		List<Move> moves1 = pieces.newPositionFor(WHITE, CastlingRights._________WC, null).computeMoves(area).moveList();
		assertTrue(moves1.contains(move("e1-c1")));
		List<Move> moves2 = pieces.newPositionFor(WHITE, CastlingRights.___________, null).computeMoves(area).moveList();
		assertFalse(moves2.contains(move("e1-c1")));
		
		pieces = Notation.parseFENPieces("8/8/8/2b5/1rq5/2k5/5P2/R3K2R");
		area = pieces.newBoard().squaresOccupiedBy(PieceType.KING.white()).asArea();
		List<Move> moves = pieces.newPositionFor(WHITE, CastlingRights.______WG_WC, null).computeMoves(area).moveList();
		assertTrue(moves.contains(move("e1-c1")));
		assertFalse(moves.contains(move("e1-g1")));
	}
	
	public void testRookAndKing() {
		Position position = Notation.parseFENPosition("1R6/8/8/8/7P/8/1p1r4/2k1K3 b - - 0 63");
		List<Move> moves = position.moves().moveList();
		assertTrue(moves.contains(move("c1-b1")));
		assertTrue(moves.contains(move("c1-c2")));
		assertTrue(moves.contains(move("d2-d1")));
		assertTrue(moves.contains(move("d2-c2")));
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
			assertEquals(Collections.emptySet(), new TreeSet<>(moves));
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
