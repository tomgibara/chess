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

import java.util.List;

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
		List<Move> moves = Rectangle.entire().on(board).availableMoves(MoveContraint.defaultWhite);
		assertEquals(20, moves.size());
	}

}
