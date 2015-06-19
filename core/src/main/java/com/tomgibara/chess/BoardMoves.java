package com.tomgibara.chess;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.function.Consumer;

public class BoardMoves extends AbstractList<Move> {

	private static final int MAX_MOVES = 256;
	
	private static ThreadLocal<Move[]> tmpArray = new ThreadLocal<Move[]>() {
		protected Move[] initialValue() {
			return new Move[MAX_MOVES];
		}
	};
	
	public final Board board;
	public final Area area;
	public final MoveContraint constraint;
	
	private Move[] moves;
	private int size = 0;
	
	BoardMoves(Board board, Area area, MoveContraint constraint) {
		this.board = board;
		this.area = area;
		this.constraint = constraint;
		
		moves = tmpArray.get();

		SquareMap<Move> checks = board.getInfo().withColour(constraint.toMove).checks();
		Squares checkers = checks.keySet();
		Squares interpose = checks.size() == 1 ? checks.get(checkers.only()).intermediateSquares : Squares.empty();
		Squares squares = area.getSquares().intersect(board.getInfo().withColour(constraint.toMove).occupiedSquares());
		Square square = squares.only();
		if (square == null) {
			squares.forEach(s -> Move.possibleMovesFrom(s).populateMoves(this, checkers, interpose));
		} else {
			Move.possibleMovesFrom(square).populateMoves(this, checkers, interpose);
		}
//Move.MoveList.bitCountCalls += squares.size();
		
		moves = Arrays.copyOfRange(moves, 0, size);
	}

	@Override
	public Move get(int index) {
		return moves[index];
	}
	
	@Override
	public boolean add(Move e) {
		if (moves.length != MAX_MOVES) throw new IllegalStateException();
		moves[size ++] = e;
		if (size == MAX_MOVES) throw new RuntimeException("MAX_MOVES met");
		return true;
	}
	
	@Override
	public void forEach(Consumer<? super Move> action) {
		for (Move move : moves) {
			action.accept(move);
		}
	}

	@Override
	public int size() {
		return size;
	}
	
}
