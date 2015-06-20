package com.tomgibara.chess.app;

import com.tomgibara.chess.Board;
import com.tomgibara.chess.Colour;
import com.tomgibara.chess.MoveConstraint;
import com.tomgibara.chess.Notation;

public class ChessApp {

	public static void main(String[] args) {

		switch (args[0]) {
		case "perf" : PerfTest.main(args); break;
		case "show" : ShowTest.main(args); break;
		default:
			System.err.println("Unknown option: " + args[0]);
			System.exit(1);
		}
	}
	
}
