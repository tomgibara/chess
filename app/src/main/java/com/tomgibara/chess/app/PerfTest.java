package com.tomgibara.chess.app;

import com.tomgibara.chess.Board;
import com.tomgibara.chess.PositionMoves;
import com.tomgibara.chess.Colour;
import com.tomgibara.chess.MoveConstraint;
import com.tomgibara.chess.Notation;

public class PerfTest {

	public static void main(String... args) {
		int repetitions = Integer.parseInt(args[1]);
		Board initial = Notation.parseFENBoard(args[2]);
		Colour colour = Colour.valueOf(args[3].toLowerCase().charAt(0));

		System.out.println("EVALUATING:");
		System.out.println(initial);
		long start = System.currentTimeMillis();
		for (int i = 0; i < repetitions; i++) {
			PositionMoves moves = initial.pieces.newBoard().newPositionFor(colour).moves();
		}
		long finish = System.currentTimeMillis();
		System.out.println(finish - start);
	}
	
}
