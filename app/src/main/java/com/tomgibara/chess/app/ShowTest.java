package com.tomgibara.chess.app;

import com.tomgibara.chess.Board;
import com.tomgibara.chess.BoardRenderer;
import com.tomgibara.chess.Colour;
import com.tomgibara.chess.MoveConstraint;
import com.tomgibara.chess.Notation;
import com.tomgibara.graphics.util.ImageUtil;

public class ShowTest {

	public static void main(String[] args) {
		Board board = Notation.parseFENBoard(args[1]);
		Colour colour = Colour.valueOf(args[2].toLowerCase().charAt(0));
		MoveConstraint constraint = MoveConstraint.defaultForColour(colour);
		int size = Integer.parseInt(args[3]);

		BoardRenderer renderer = new BoardRenderer(size);
		renderer.render(board);
		renderer.render(board.computeMoves(constraint));
		ImageUtil.showImage("Result", renderer.getImage());
	}
	
}
