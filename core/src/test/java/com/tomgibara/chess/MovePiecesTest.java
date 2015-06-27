package com.tomgibara.chess;

import junit.framework.TestCase;

public class MovePiecesTest extends TestCase {

	public void testLookup() {
		for (PieceType moved : PieceType.values()) {
			for (PieceType captured : PieceType.values()) {
				if (captured == PieceType.KING) captured = null;
				MovePieces pieces = MovePieces.regular(moved, captured);
				assertEquals(moved, pieces.moved);
				assertEquals(captured, pieces.captured);
			}
		}
	}
	
}
