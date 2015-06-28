package com.tomgibara.chess;

import com.tomgibara.graphics.util.ImageUtil;

public class TestRender {

	public static void main(String[] args) {
		Position position = Notation.parseFENPosition("8/3k2Q1/8/4qn2/8/3K4/8/8");
		//Board board = Notation.parseFENBoard("8/3k2Q1/8/4qn2/8/3K4/8/8");
		//Board board = Notation.parseFENBoard("8/8/8/2b5/1rq5/2k5/5P2/R3K2R");
		//Board board = Notation.parseFENBoard("4k3/8/8/q7/8/r3b3/3PP3/R3Kb1r");
		//Board board = Notation.parseFENBoard("7r/1k2pn1R/8/1r1p4/8/3K1B1q/8/1R6");
		//Board board = Notation.parseFENBoard("r1b1kb1r/pp1n1p1p/2p1p1pn/q3N3/2BP1B2/2N5/PPP2PPP/R2QK2R");
		//Board board = Notation.parseFENBoard("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R");
		BoardRenderer renderer = new BoardRenderer(800, false);
		renderer.render(position.pieces().newBoard());
		renderer.render(position.moves());
		ImageUtil.showImage("Result", renderer.getImage());
	}
	
}
