package com.tomgibara.chess;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoardMoves extends AbstractList<Move> {

	private static final int MAX_MOVES = 256;

	private static final Comparator<? super Move> moveDistComp = (m1, m2) -> m1.spannedSquares.size() - m2.spannedSquares.size();

	private static ThreadLocal<Move[]> tmpArray = new ThreadLocal<Move[]>() {
		protected Move[] initialValue() {
			return new Move[MAX_MOVES];
		}
	};
	
	private static SquareMap<List<Move>> newMap() {
		return new SquareMap<List<Move>>(new List[64], 0);
	}
	
	public final Board board;
	public final Area area;
	public final MoveConstraint constraint;
	
	private Move[] moves;
	private int size = 0;
	
	BoardMoves(Board board, Area area, MoveConstraint constraint) {
		this.board = board;
		this.area = area;
		this.constraint = constraint;
		
		moves = tmpArray.get();

		SquareMap<Move> checks = board.withColour(constraint.toMove).checks();
		Squares checkers = checks.keySet();
		Squares interpose = checks.size() == 1 ? checks.get(checkers.only()).intermediateSquares : Squares.empty();
		Squares squares = area.getSquares().intersect(board.withColour(constraint.toMove).occupiedSquares());
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
	
	//TODO make efficient?
	public List<Move> movesFrom(Square square) {
		if (square == null) throw new IllegalArgumentException("null square");
		return Arrays.stream(moves).filter(m -> m.from == square).sorted(moveDistComp).collect(Collectors.toList());
	}
	
	public SquareMap<List<Move>> movesByOriginSquare() {
		SquareMap<List<Move>> map = Arrays.stream(moves).collect(Collectors.groupingBy(m -> m.from, BoardMoves::newMap, Collectors.toList()));
		//TODO how to combine these in one stream operation?
		map.values().forEach( l -> l.sort(moveDistComp) );
		return map;
	}
	
}
