package com.tomgibara.chess;

import static com.tomgibara.chess.Colour.BLACK;
import static com.tomgibara.chess.Colour.WHITE;
import static com.tomgibara.chess.Square.at;
import junit.framework.TestCase;

public class PiecesTest extends TestCase {

	public void testMove() {
		Pieces initial = Board.initial().pieces;
		Pieces pieces = initial.mutableCopy();
		pieces.make(Colour.WHITE, Move.move("e2-e4"), MovePieces.regular(PieceType.PAWN, null));
		assertFalse(pieces.equals(initial));
	}
	
	public void testEnPassant() {
		Position position = Notation.parseFENPosition("7k/8/1p6/pP6/8/5n1b/8/7K w - a6 0 1");
		testAllTakeBacks(position);
		PositionMoves moves = position.moves();
		assertEquals(1, moves.moveCount());
		Pieces pieces = position.pieces().mutableCopy();
		pieces.make(WHITE, moves.move(0), moves.pieces(0));
		assertNull(pieces.get(at("a5")));
	}

	public void testTakeBacks() {
		testAllTakeBacks("2K5/1B4N1/4k3/4P2Q/8/8/8/8");
		testAllTakeBacks("4k3/8/8/8/pP6/8/8/4K3");
		testAllTakeBacks("4k3/8/8/q7/8/r3b3/3PP3/R3Kb1r");
		testAllTakeBacks("kn6/P6P/8/8/8/b1n5/8/K7");
	}

	private void testAllTakeBacks(String fen) {
		Board board = Notation.parseFENPieces(fen).newBoard();
		testAllTakeBacks(board, WHITE);
		testAllTakeBacks(board, BLACK);
	}

	private void testAllTakeBacks(Board board, Colour colour) {
		if (board.withColour(colour.opposite()).checks().isEmpty()) {
			testAllTakeBacks( board.pieces.newPositionFor(colour) );
		}
	}
	
	private void testAllTakeBacks(Position position) {
		Pieces initial = position.pieces();
		Pieces pieces = initial.mutableCopy();
		PositionMoves moves = position.moves();
		Colour colour = position.toMove;
		moves.forEach((m,p) -> {
			pieces.make(colour, m, p);
			String desc = "Move: " + m + " Pieces: " + p;
			assertFalse(desc, pieces.equals(initial));
			pieces.takeBack(colour, m, p);
			assertEquals(desc, initial, pieces);
		});
	}
	
}
