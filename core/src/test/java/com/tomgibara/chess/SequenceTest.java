package com.tomgibara.chess;

import static com.tomgibara.chess.Move.move;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

public class SequenceTest extends TestCase {

	public void testBasic() {
		Position position = new Sequence().position();
		for (int i = 0; i < 70; i++) {
			position = position.moves().make(0);
		}
	}

	public void testRandomized() {
		Random r = new Random(0L);
		for (int j = 0; j < 1000; j++) {
			Position position = new Sequence().position();
			for (int i = 0; i < 30; i++) {
				int count = position.moves().moveCount();
				if (count == 0) break;
				int index = r.nextInt(count);
				Position subsequent = position.moves().make(index);
				assertEquals(index, position.moveIndex());
				position = subsequent;
			}
		}
	}

	public void testIndexing() {
		Sequence sequence = new Sequence().position()
				.makeMove("f3").makeMove("e5")
				.makeMove("g4").makeMove("Qh4#")
				.sequence;
		assertEquals(5, sequence.length());
		assertEquals(Board.initial().pieces, sequence.position(0).pieces());
		assertEquals(Notation.parseFENPieces("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR"), sequence.position(4).pieces());
	}
	
	public void testKIA() {
		Position position = new Sequence().position()
				.makeMove(move("e2-e4")).makeMove(move("e7-e6"))
				.makeMove(move("d2-d3")).makeMove(move("d7-d5"))
				.makeMove(move("b1-d2")).makeMove(move("g8-f6"))
				.makeMove(move("g1-f3")).makeMove(move("c7-c5"))
				.makeMove(move("g2-g3")).makeMove(move("b8-c6"))
				.makeMove(move("f1-g2")).makeMove(move("f8-e7"))
				.makeMove(move("e1-g1")).makeMove(move("e8-g8"));

		assertEquals(kiaExpected(), position);

		AtomicInteger counter = new AtomicInteger();
		position.sequence.forEach(p -> counter.incrementAndGet());
		assertEquals(15, counter.get());
	}

	public void testKIA2() {
		Position position = new Sequence().position()
				.makeMove("e4")  .makeMove("e6")
				.makeMove("d3")  .makeMove("d5")
				.makeMove("Nd2") .makeMove("Nf6")
				.makeMove("Ngf3").makeMove("c5")
				.makeMove("g3")  .makeMove("Nc6")
				.makeMove("Bg2") .makeMove("Be7")
				.makeMove("O-O") .makeMove("O-O")
				;

		assertEquals(kiaExpected(), position);
	}
	
	private Position kiaExpected() {
		return Notation.parseFENPosition("r1bq1rk1/pp2bppp/2n1pn2/2pp4/4P3/3P1NP1/PPPN1PBP/R1BQ1RK1 w - - 5 8");
	}
}
