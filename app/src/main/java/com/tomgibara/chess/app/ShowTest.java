package com.tomgibara.chess.app;

import java.util.Arrays;

import com.tomgibara.chess.Board;
import com.tomgibara.chess.BoardRenderer;
import com.tomgibara.chess.Colour;
import com.tomgibara.chess.MoveConstraint;
import com.tomgibara.chess.Notation;
import com.tomgibara.chess.Position;
import com.tomgibara.graphics.util.ImageUtil;

public class ShowTest {

	public static void main(String[] args) {
		int size;
		{
			int length = args.length;
			size = Integer.parseInt(args[length - 1]);
			args = Arrays.copyOfRange(args, 1, length -1);
		}
		Position position = Notation.parseFENPosition(args);

		BoardRenderer renderer = new BoardRenderer(size, true);
		renderer.render(position.pieces().newBoard());
		renderer.render(position.moves());
		ImageUtil.showImage("Result", renderer.getImage());
	}
	
}
