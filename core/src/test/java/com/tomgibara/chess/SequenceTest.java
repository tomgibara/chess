package com.tomgibara.chess;

import junit.framework.TestCase;

public class SequenceTest extends TestCase {

	public void testBasic() {
		
		Position position = new Sequence().position();
		for (int i = 0; i < 70; i++) {
			position = position.moves().make(0);
		}
		System.out.println( position.pieces().newBoard() );
	}
	
}
