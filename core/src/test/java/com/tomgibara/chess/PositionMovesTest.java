package com.tomgibara.chess;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class PositionMovesTest extends TestCase {

	public void testNotation() {
		for (String n : notation(new Sequence().position())) {
			assertTrue(n.indexOf('x') == -1);
		}
	}

	public void testAmbiguousNotation() {
		Set<String> notation = notation( Notation.parseFENPosition("6bR/5k2/8/8/8/8/N4p2/KN4bR w - - 0 1") );
		Set<String> knights = notation("Nb4,Nac3,Nbc3,Nd2");
		assertTrue(notation.containsAll(knights));
		Set<String> rooks = notation("R8h7,R1h7,R8h6,R1h6,R8h5,R1h5,R8h4,R1h4");
		assertTrue(notation.containsAll(rooks));
		Set<String> captures = notation("Rxg8,Rxg1");
		assertTrue(notation.containsAll(captures));
	}

	public void testPromotionNotation() {
		Set<String> notation = notation( Notation.parseFENPosition("kn6/P6P/8/8/8/b1n5/8/K7 w - - 0 1") );
		assertTrue(notation.containsAll(notation("axb8=Q,h8=R")));
		assertEquals(8, notation.size());
	}

	private static Set<String> notation(String str) {
		return new HashSet<>(Arrays.asList(str.split(",")));
	}
	
	private static Set<String> notation(Position position) {
		Set<String> set = new HashSet<>();
		PositionMoves moves = position.moves();
		int count = moves.moveCount();
		for (int i = 0; i < count; i++) {
			set.add( moves.notation(i) );
		}
		return set;
	}
}
