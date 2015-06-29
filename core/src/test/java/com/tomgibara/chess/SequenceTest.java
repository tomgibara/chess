package com.tomgibara.chess;

import static com.tomgibara.chess.Move.move;
import junit.framework.TestCase;

public class SequenceTest extends TestCase {

	public void testBasic() {
		Position position = new Sequence().position();
		for (int i = 0; i < 70; i++) {
			position = position.moves().make(0);
		}
		System.out.println( position.pieces().newBoard() );
	}

	public void testKIA() {
		Position position = new Sequence().position()
				.makeMove(move("e2-e4"))
				.makeMove(move("e7-e6"))
				.makeMove(move("d2-d3"))
				.makeMove(move("d7-d5"))
				.makeMove(move("b1-d2"))
				.makeMove(move("g8-f6"))
				.makeMove(move("g1-f3"))
				.makeMove(move("c7-c5"))
				.makeMove(move("g2-g3"))
				.makeMove(move("b8-c6"))
				.makeMove(move("f1-g2"))
				.makeMove(move("f8-e7"))
				.makeMove(move("e1-g1"))
				.makeMove(move("e8-g8"));
		
		Position expected = Notation.parseFENPosition("r1bq1rk1/pp2bppp/2n1pn2/2pp4/4P3/3P1NP1/PPPN1PBP/R1BQ1RK1 w - - 5 8");
		assertEquals(expected, position);
	}

}
