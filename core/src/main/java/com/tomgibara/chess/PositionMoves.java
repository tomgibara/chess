package com.tomgibara.chess;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PositionMoves {

	private static final int PIECE_BITS = 6;
	private static final int PIECE_MASK = (1 << PIECE_BITS) - 1;
	
	private static final Comparator<? super Move> moveDistComp = (m1, m2) -> m1.spannedSquares.size() - m2.spannedSquares.size();

	private static SquareMap<List<Move>> newMap() {
		return new SquareMap<List<Move>>(new List[64], 0);
	}
	
	static Move codeMove(int code) {
		return Move.forOrdinal(code >> PIECE_BITS);
	}
	
	static MovePieces codePieces(int code) {
		return MovePieces.from(code & PIECE_MASK);
	}
	
	public static int code(Move move, MovePieces pieces) {
		return (move.ordinal << PIECE_BITS) | pieces.ordinal;
	}
	
	public static int code(Move move) {
		return move.ordinal << PIECE_BITS;
	}
	
	public static boolean isCapture(Move move, MovePieces pieces) {
		return pieces.moved == PieceType.PAWN ? move.isPawnCapture() : pieces.captured != null;
	}

	public final Position position;
	public final Area area;
	private final int[] codes;
	private MoveList moveList = null;

	PositionMoves(Position position, Board board, Area area) {
		this.position = position;
		this.area = area;
		this.codes = new MovePopulator(board, position.constraint, area).moves();
	}
	
	public int moveCount() {
		return codes.length;
	}
	
	public Move move(int index) {
		return codeMove(codes[index]);
	}

	public MovePieces pieces(int index) {
		return codePieces(codes[index]);
	}
	
	public String notation(int index) {
		int code = codes[index];
		Move move = codeMove(code);
		MovePieces pieces = codePieces(code);
		StringBuffer sb = new StringBuffer();
		PieceType type = pieces.moved;
		if (type == PieceType.PAWN) {
			if (move.isPawnCapture()) sb.append(move.from.file).append('x');
			sb.append(move.to);
			if (move.isPromotion()) sb.append('=').append(pieces.promotion.character);
		} else {
			sb.append(type.character);
			Move m = ambiguatingMove(move, pieces);
			if (m != null) {
				Square s1 = move.from;
				Square s2 = m.from;
				if (s1.file != s2.file) {
					sb.append(s1.file.character);
				} else if (s1.rank != s2.rank) {
					sb.append(s1.rank.character);
				} else {
					sb.append(s1);
				}
			}
			if (pieces.captured != null) sb.append('x');
			sb.append(move.to);
		}
		//TODO
		//if (isCheck()) sb.append('+');
		return sb.toString();
	}
	
	public Position make(int index) {
		return position.makeMove(codes[index]);
	}
	
	public List<Move> moveList() {
		return moveList == null ? moveList = new MoveList(codes) : moveList;
	}
	
	public void forEach(BiConsumer<Move, MovePieces> action) {
		for (int i = 0; i < codes.length; i++) {
			int code = codes[i];
			action.accept(codeMove(code), codePieces(code));
		}
	}

	//TODO make efficient?
	public List<Move> movesFrom(Square square) {
		if (square == null) throw new IllegalArgumentException("null square");
		return moveStream().filter(m -> m.from == square).sorted(moveDistComp).collect(Collectors.toList());
	}

	public SquareMap<List<Move>> movesByOriginSquare() {
		SquareMap<List<Move>> map = moveStream().collect(Collectors.groupingBy(m -> m.from, PositionMoves::newMap, Collectors.toList()));
		//TODO how to combine these in one stream operation?
		map.values().forEach( l -> l.sort(moveDistComp) );
		return map;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < codes.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(notation(i));
		}
		return sb.toString();
	}
	
	Position make(Move move) {
		int code = code(move);
		int i = Arrays.binarySearch(codes, code);
		if (i >= 0) return position.makeMove(code);
		i = -1 - i;
		if (i >= codes.length) throw new IllegalArgumentException("invalid move: " + move);
		int candidate = codes[i++];
		if ((candidate & ~PIECE_MASK) != code) throw new IllegalArgumentException("invalid move: " + move);
		if (i >= codes.length || (codes[i] & ~PIECE_MASK) != code) return position.makeMove(candidate);
		throw new IllegalArgumentException("ambiguous move: " + move);
	}
	
	private Stream<Move> moveStream() {
		return Arrays.stream(codes).mapToObj(c -> codeMove(c));
	}
	
	private Move ambiguatingMove(Move move, MovePieces pieces) {
		PieceType moved = pieces.moved;
		Square to = move.to;
		Square from = move.from;
		for (int i = 0; i < codes.length; i++) {
			int code = codes[i];
			Move m = codeMove(code);
			if (
					m.to == to &&
					m.from != from &&
					codePieces(code).moved == moved
					)
				return m;
		}
		return null;
	}
	
	private static class MovePopulator implements Consumer<Square> {

		private static final int MAX_MOVES = 256;

		private static ThreadLocal<int[]> tmpCodes = new ThreadLocal<int[]>() {
			protected int[] initialValue() {
				return new int[MAX_MOVES];
			}
		};
		
		private final Board board;
		private final MoveConstraint constraint;
		private final Squares checkers;
		private final Squares interpose;
		
		int count = 0;
		int[] codes = tmpCodes.get();
		
		MovePopulator(Board board, MoveConstraint constraint, Area area) {
			this.board = board;
			this.constraint = constraint;

			SquareMap<Move> checks = board.withColour(constraint.toMove).checks();
			this.checkers = checks.keySet();
			this.interpose = checks.size() == 1 ? checks.get(checkers.only()).intermediateSquares : Squares.empty();

			Squares squares = area.getSquares().intersect(board.withColour(constraint.toMove).occupiedSquares());
			Square square = squares.only();
			if (square == null) {
				squares.forEach(this);
			} else {
				accept(square);
			}

		}

		@Override
		public void accept(Square s) {
			count = Move.possibleMovesFrom(s).populateMoves(codes, count, board, constraint, checkers, interpose);
		}
		
		int[] moves() {
			return Arrays.copyOfRange(codes, 0, count);
		}

	}
	
	private static class MoveList extends AbstractList<Move> {
		
		private final int[] codes;
		
		MoveList(int[] codes) {
			this.codes = codes;
		}
		
		@Override
		public Move get(int index) {
			return codeMove(codes[index]);
		}
		
		@Override
		public int size() {
			return codes.length;
		}
		
		@Override
		public boolean contains(Object o) {
			return indexOf(o) != -1;
		}
		
		@Override
		public boolean isEmpty() {
			return codes.length == 0;
		}
		
		@Override
		public void forEach(Consumer<? super Move> action) {
			for (int code : codes) {
				action.accept(codeMove(code));
			}
		}
		
		@Override
		public int indexOf(Object o) {
			if (!(o instanceof Move)) return -1;
			Move move = (Move) o;
			int ordinal = move.ordinal;
			for (int i = 0; i < codes.length; i++) {
				int code = codes[i];
				if ((code >> PIECE_BITS) == ordinal) return i;
			}
			return -1;
		}

	}
	
}
