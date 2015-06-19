package com.tomgibara.chess;

import java.util.List;

import junit.framework.TestCase;

public class MoveTest extends TestCase {

	public void testAllMoves() {
		for (Move move : Move.allMoves()) {
			assertNotNull(move);
			assert(move.reverse().reverse() == move);
		}
	}
	
	public void testPossibleMoves() {
		for (Move move : Move.possibleMoves()) {
			assertTrue(move.isPossible());
		}
	}
	
	public void testMovesToAndFromSquare() {
		Square sqr = Square.at(File.FL_B, Rank.RK_7);
		{
			List<Move> moves = Move.possibleMovesFrom(sqr);
			assertFalse(moves.isEmpty());
			moves.forEach( m-> assertTrue(Move.possibleMovesTo(sqr).contains(m.reverse())) );
		}
		{
			List<Move> moves = Move.possibleMovesTo(sqr);
			assertFalse(moves.isEmpty());
			moves.forEach( m-> assertTrue(Move.possibleMovesFrom(sqr).contains(m.reverse())) );
		}
	}
	
	public void testNoKnightOverlap() {
		for (Move move : Move.possibleMoves()) {
			assertFalse( move.isPossibleFor(Piece.KNIGHT) && move.isPossibleFor(Piece.QUEEN) );
		}
	}
	
}
