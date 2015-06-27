package com.tomgibara.chess;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

//TODO allow move sorting?
public class PositionMoves {

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
	
	public final Position position;
	public final Area area;
	
	private Move[] movesArray;
	private int movesSize = 0;

	private List<Move> moves;
	private MoveList moveList;
	
	PositionMoves(Position position, Area area) {
		this.position = position;
		this.area = area;
		
		movesArray = tmpArray.get();

		Board board = position.board;
		MoveConstraint constraint = position.constraint;
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
		
		movesArray = Arrays.copyOfRange(movesArray, 0, movesSize);
	}

	//TODO needs a better name
	public List<Move> moves() {
		return moves == null ? moves = Collections.unmodifiableList(Arrays.asList(movesArray)) : moves;
	}
	
	public MoveList list() {
		return moveList == null ? moveList = new MoveList(position, movesArray) : moveList;
	}
	
	//TODO make efficient?
	public List<Move> movesFrom(Square square) {
		if (square == null) throw new IllegalArgumentException("null square");
		return Arrays.stream(movesArray).filter(m -> m.from == square).sorted(moveDistComp).collect(Collectors.toList());
	}
	
	public SquareMap<List<Move>> movesByOriginSquare() {
		SquareMap<List<Move>> map = Arrays.stream(movesArray).collect(Collectors.groupingBy(m -> m.from, PositionMoves::newMap, Collectors.toList()));
		//TODO how to combine these in one stream operation?
		map.values().forEach( l -> l.sort(moveDistComp) );
		return map;
	}
	
	void record(Move move) {
		if (movesArray.length != MAX_MOVES) throw new IllegalStateException();
		movesArray[movesSize ++] = move;
		if (movesSize == MAX_MOVES) throw new RuntimeException("MAX_MOVES met");
	}

	public static final class MoveList extends AbstractList<PositionMove> {
		
		private int size = 0;
		final PositionMove[] array;

		MoveList(Position position, Move[] moves) {
			int capacity = moves.length;
			for (Move move : moves) {
				if (move.isPromotion(position)) capacity += 3; 
			}
			array = new PositionMove[capacity];
			PositionMove.transform(position, moves, array);
		}
		
		//TODO theoretically, there could be more than one ambiguity
		public PositionMove ambiguatingMove(PositionMove move) {
			for (PositionMove candidate : array) {
				if (
						candidate.piece()   == move.piece() &&
						candidate.move.to   == move.move.to &&
						candidate.move.from != move.move.from
						) {
					return candidate;
				}
			}
			return null;
		}
		
		@Override
		public boolean isEmpty() {
			return size != 0;
		}
		
		@Override
		public boolean add(PositionMove e) {
			array[size++] = e;
			return true;
		}
		
		@Override
		public PositionMove get(int index) {
			return array[index];
		}
		@Override
		public int size() {
			return size;
		}
		
		@Override
		public void forEach(Consumer<? super PositionMove> action) {
			for (PositionMove move : array) {
				action.accept(move);
			}
		}

	}
	
}
