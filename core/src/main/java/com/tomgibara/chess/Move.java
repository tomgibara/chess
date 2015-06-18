package com.tomgibara.chess;

import static com.tomgibara.chess.File.FL_C;
import static com.tomgibara.chess.File.FL_E;
import static com.tomgibara.chess.File.FL_G;
import static com.tomgibara.chess.Rank.RK_1;
import static com.tomgibara.chess.Rank.RK_2;
import static com.tomgibara.chess.Rank.RK_7;
import static com.tomgibara.chess.Rank.RK_8;
import static java.lang.Math.abs;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class Move {

	private static final int WHITE_PAWN     = 0b000000001;
	private static final int BLACK_PAWN     = 0b000000010;
	private static final int PAWN_CAPTURE   = 0b000000100;
	private static final int KNIGHT         = 0b000001000;
	private static final int BISHOP         = 0b000010000;
	private static final int ROOK           = 0b000100000;
	private static final int KING           = 0b001000000;
	private static final int WHITE_CASTLE   = 0b010000000;
	private static final int BLACK_CASTLE   = 0b100000000;
	
	private static final int WHITE_KING = KING | WHITE_CASTLE;
	private static final int BLACK_KING = KING | BLACK_CASTLE;
	private static final int EITHER_KING = KING | WHITE_CASTLE | BLACK_CASTLE;
	private static final int EITHER_PAWN = WHITE_PAWN | BLACK_PAWN;
	private static final int QUEEN = BISHOP | ROOK;
	private static final int CASTLE = WHITE_CASTLE | BLACK_CASTLE;

	private static int ordinal(Square from, Square to) {
		return from.ordinal << 6 | to.ordinal;
	}
	
	private static final Move[] moves = new Move[4096];
	
	static {
		for (int from = 0; from < 64; from++) {
			Square fromSq = Square.at(from);
			for (int to = 0; to < 64; to++) {
				Square toSq = Square.at(to);
				Move move = new Move(fromSq, toSq);
				moves[ move.ordinal ] = move;
			}
		}
		for (Move move : moves) {
			move.reverse = moves[ ordinal(move.to, move.from) ];
		}
	}

	private static final List<Move> allMoves = Collections.unmodifiableList( Arrays.asList(moves) );
	
	private static final Collector<Move, ?, List<Move>> dummy = Collectors.toCollection(ArrayList<Move>::new);
	private static final Collector<Move, ?, List<Move>> moveCollector = Collectors.collectingAndThen(dummy, Collections::unmodifiableList);

	private static final List<Move> possibleMoves;
	
	private static final Map<Piece, List<Move>> pieceMoves;

	private static final MoveList[] movesFrom = new MoveList[64];
	private static final MoveList[] movesTo = new MoveList[64];

	static {
		possibleMoves = allMoves.stream().filter( m -> m.isPossible() ).collect(moveCollector);
		EnumMap<Piece, List<Move>> map = new EnumMap<Piece, List<Move>>(Piece.class);
		map.entrySet().stream().forEach(e -> e.setValue(new ArrayList<Move>()));
		for (Piece piece : Piece.values()) {
			List<Move> list = allMoves.stream().filter( m -> m.isPossibleFor(piece) ).collect(moveCollector);
			map.put(piece, list);
		}
		pieceMoves = Collections.unmodifiableMap(map);
		for (int i = 0; i < movesFrom.length; i++) {
			movesFrom[i] = new MoveList(i, false);
			movesTo[i] = new MoveList(i, true);
		}
	}
	
	public static List<Move> allMoves() {
		return allMoves;
	}
	
	public static List<Move> possibleMoves() {
		return possibleMoves;
	}
	
	//TODO use ordered sets?
	public static List<Move> possibleMovesFor(Piece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		return pieceMoves.get(piece);
	}
	
	public static MoveList possibleMovesFrom(Square square) {
		if (square == null) throw new IllegalArgumentException("null square");
		return movesFrom[square.ordinal];
	}
	
	public static MoveList possibleMovesTo(Square square) {
		if (square == null) throw new IllegalArgumentException("null square");
		return movesTo[square.ordinal];
	}
	
	public static Move forOrdinal(int ordinal) {
		if (ordinal < 0 || ordinal >= 4096) throw new IllegalArgumentException("invalid ordinal");
		return moves[ordinal];
	}
	
	public static Move between(Square from, Square to) {
		if (from == null) throw new IllegalArgumentException("null from");
		if (to == null) throw new IllegalArgumentException("null to");
		return moves[ ordinal(from, to) ];
	}
	
	public static Move move(String str) {
		if (str == null) throw new IllegalArgumentException("null str");
		if (str.length() != 5 || str.charAt(2) != '-') throw new IllegalArgumentException();
		Square from = Square.at(str.substring(0, 2));
		Square to   = Square.at(str.substring(3, 5));
		return moves[ ordinal(from, to) ];
	}
	
	private static final SquareMap<Move> emptyMap = newMoveMap().immutable();
	
	public static SquareMap<Move> emptyMap() { return emptyMap; }
	
	public static SquareMap<Move> newMoveMap() {
		return new SquareMap<>(new Move[64], 0);
	}
	
	public final int ordinal;
	public final Square from;
	public final Square to;
	private final int flags;
	//TODO ideally needs a list
	public final Squares intermediateSquares;
	public final Squares spannedSquares;
	private Move reverse = null;
	
	private Move(Square from, Square to) {
		ordinal = ordinal(from, to);
		this.from = from;
		this.to = to;
		
		int df = to.file.difference(from.file);
		int dr = to.rank.difference(from.rank);
		
		int flags = 0;
		Squares inter = Squares.empty();
		if (df != 0 || dr != 0) {
			int adf = abs(df);
			int adr = abs(dr);
			
			boolean whitePawn =
					(from.rank != RK_1 && dr == 1 && adf <= 1) ||
					(from.rank == RK_2 && dr == 2 && df  == 0);
			boolean blackPawn =
					(from.rank != RK_8 && dr == -1 && adf <= 1) ||
					(from.rank == RK_7 && dr == -2 && df  == 0);
			boolean pawnCapture = (whitePawn || blackPawn) && adf != 0;
			boolean knight = adf < 3 && adr < 3 && adf + adr == 3;
			boolean bishop = df == dr || df == -dr;
			boolean rook = df == 0 || dr == 0;
			boolean king = adf <= 1 && adr <= 1;
			boolean whiteCastle =
					from == Square.at(FL_E, RK_1) &
					( to == Square.at(FL_G, RK_1) ||
					  to == Square.at(FL_C, RK_1) );
			boolean blackCastle =
					from == Square.at(FL_E, RK_8) &
					( to == Square.at(FL_G, RK_8) ||
					  to == Square.at(FL_C, RK_8) );

			if (whitePawn)   flags |= WHITE_PAWN;
			if (blackPawn)   flags |= BLACK_PAWN;
			if (pawnCapture) flags |= PAWN_CAPTURE;
			if (knight)      flags |= KNIGHT;
			if (bishop)      flags |= BISHOP;
			if (rook)        flags |= ROOK;
			if (king)        flags |= KING;
			if (whiteCastle) flags |= WHITE_CASTLE;
			if (blackCastle) flags |= BLACK_CASTLE;
			
			if (!king && !knight) { // never intermediate 
				List<Square> list = new ArrayList<Square>(Math.max(adf, adr) - 1);
				File file = from.file;
				Rank rank = from.rank;
				File toFile = to.file;
				Rank toRank = to.rank;
				while (true) {
					file = file.towards(toFile);
					rank = rank.towards(toRank);
					if (file == toFile && rank == toRank) break;
					list.add(file.intersect(rank));
				}
				inter = Squares.immutable(list);
			}
		}

		this.flags = flags;
		this.intermediateSquares = inter;
		MutableSquares spanned = new MutableSquares(inter);
		spanned.add(from);
		spanned.add(to);
		this.spannedSquares = Squares.immutable(spanned);
	}
	
	public boolean isCastling() {
		return anySet(CASTLE);
	}
	
	public boolean isPawnCapture() {
		return anySet(PAWN_CAPTURE);
	}
	
	public boolean isPossibleFor(Piece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		switch (piece) {
		case BISHOP: return anySet(BISHOP);
		case KING: return anySet(EITHER_KING);
		case KNIGHT: return anySet(KNIGHT);
		case PAWN: return anySet(EITHER_PAWN);
		case QUEEN: return anySet(QUEEN);
		case ROOK: return anySet(ROOK);
		default: throw new IllegalStateException();
		}
	}
	
	public boolean isPossibleFor(ColouredPiece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		switch (piece) {
		case BLACK_PAWN : return anySet(BLACK_PAWN);
		case WHITE_PAWN : return anySet(WHITE_PAWN);
		case BLACK_KING : return anySet(BLACK_KING);
		case WHITE_KING : return anySet(WHITE_KING);
		default: return isPossibleFor(piece.piece);
		}
	}
	
	public Pieces possiblePieces() {
		return Pieces.containing(
				isPossibleFor(Piece.PAWN  ),
				isPossibleFor(Piece.KNIGHT),
				isPossibleFor(Piece.BISHOP),
				isPossibleFor(Piece.ROOK  ),
				isPossibleFor(Piece.QUEEN ),
				isPossibleFor(Piece.KING  )
				);
	}
	
	public boolean isPossible() {
		return flags != 0;
	}
	
	public Move reverse() {
		return reverse;
	}
	
	public Move inducedRookMove() {
		if (!isCastling()) return null;
		Rank rank = to.rank;
		File fromFile = to.file.nearestFlank();
		File toFile = from.file.towards(fromFile);
		return between(rank.intersect(fromFile), rank.intersect(toFile));
	}
	
	public Interposition interpose(Square square) {
		if (square == null) throw new IllegalArgumentException("null square");
		if (!intermediateSquares.contains(square)) throw new IllegalArgumentException("square not intermediate");
		return new Interposition(this, square);
	}
	
	@Override
	public int hashCode() {
		return ordinal;
	}
	
	@Override
	public String toString() {
		return from + "-" + to;
	}

	private boolean anySet(int mask) {
		return (flags & mask) != 0;
	}
	
	public static class MoveList extends AbstractList<Move> {
		
		public final Square square;
		private final boolean reverse;
		private final int ordinal;
		private final long bits;
		private final int size;
		private final SquareMap<Move> map;
		
		MoveList(int ordinal, boolean reverse) {
			this.ordinal = ordinal;
			this.reverse = reverse;
			square = Square.at(ordinal);
			
			long bits = 0L;
			long bit = 1L;
			int size = 0;
			for (int offset = 0; offset < 64; offset++) {
				int p = ptr(offset);
				if (moves[p].isPossible()) {
					bits |= bit;
					size ++;
				}
				bit <<= 1;
			}
			this.bits = bits;
			this.size = size;
			
			map = new SquareMap<Move>(new SquareMap.Store<Move>() {

				@Override
				public Class<? extends Move> valueType() { return Move.class; }

				@Override
				public Move get(int index) { return moves[ptr(index)]; }

				@Override
				public int size() { return MoveList.this.size; }
			});
		}
		
		public SquareMap<Move> asMap() {
			return map;
		}
		
		@Override
		public Move get(int index) {
			if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
			//TODO modify to use binary lookup
			for (int offset = 0; offset < 64; offset++) {
				if (((bits >> offset) & 1L) == 1L) {
					if (index == 0) return moves[ptr(offset)];
					index --;
				}
			}
			throw new IllegalStateException("" + index);
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Move)) return false;
			Move m = (Move) o;
			if (!m.isPossible()) return false;
			int offset = off(m.ordinal);
			return offset >= 0 && offset < 64;
		}

		@Override
		public int indexOf(Object o) {
			if (!(o instanceof Move)) return -1;
			Move m = (Move) o;
			if (!m.isPossible()) return -1;
			int offset = off(m.ordinal);
			int index = size - Long.bitCount(bits & (-1L << offset));
			return index;
		}

		@Override
		public int lastIndexOf(Object o) {
			return indexOf(o);
		}

		@Override
		public boolean isEmpty() {
			return bits == 0L;
		}

		@Override
		public int size() {
			return size;
		}

		//TODO would like to eliminate branch - subclass instead?
		private int ptr(int off) {
			return reverse ? (off << 6) + ordinal : (ordinal << 6) + off;
		}
		
		private int off(int ptr) {
			return reverse ? (ptr >> 6) & 0x3f : ptr & 0x3f;
		}

		void populateMoves(BoardMoves moves, Squares checkers, Squares interpose) {
			Board board = moves.board;
			MoveContraint constraint = moves.constraint;
			SquareMap<ColouredPiece> pieces = board.pieces;
			ColouredPiece piece = pieces.get(square);
			Squares occupied = pieces.keySet();

			if (checkers.isEmpty() || piece.piece == Piece.KING) {
				//TODO optimize
				for (Move move : this) { // regular case
					if (!move.isPossibleFor(piece)) continue; // piece cannot move in that way
					ColouredPiece target = pieces.get(move.to);
					if (target != null && target.colour == piece.colour) continue; // piece cannot capture same colour
					if (piece.piece == Piece.PAWN && move.isPawnCapture() == (target == null && move.to != constraint.enPassantSqr)) continue; // pawns must capture to move diagonally
					if (occupied.intersects(move.intermediateSquares)) continue; // one or more pieces interposed
					if (piece.piece == Piece.KING) {
						Colour attackColour = piece.colour.opposite();
						if (isAttackedAfter(pieces, occupied, move, attackColour)) continue; // king cannot move into check
						if (move.isCastling()) {
							if (!constraint.castlingSquares.contains(move.to)) continue; // cannot castle invalidly
							if (!checkers.isEmpty()) continue; // cannot castle out of check
							Move rookMove = move.inducedRookMove();
							if (pieces.get(rookMove.from) != Piece.ROOK.coloured(constraint.toMove)) continue; // rook must be present
							if (occupied.intersects( rookMove.intermediateSquares )) continue; // path must be clear for rook
							if (isAttackedAfter(pieces, occupied, rookMove.from.to(move.intermediateSquares.only()), attackColour)) continue; // cannot pass through check
						}
					} else {
						Interposition pin = board.getInfo().withColour(piece.colour).pinnedToKing().get(square);
						if (pin != null && !pin.move.spannedSquares.contains(move.to)) continue; // piece breaks pin on king
					}
					moves.add(move);
				}
			} else { // check
				// we can try to interpose
				for (Square i : interpose) {
					Move move = Move.between(square, i);
					if (move.isPossibleFor(piece) && !occupied.intersects(move.intermediateSquares)) {
						moves.add(move);
					}
				}
				// we can try to capture
				Square c = checkers.only();
				if (c != null) {
					Move move = Move.between(square, c);
					if (move.isPossibleFor(piece) && !occupied.intersects(move.intermediateSquares)) {
						moves.add(move);
					}
				}
			}
		}

		private boolean isAttackedAfter(SquareMap<ColouredPiece> pieces, Squares occupied, Move m, Colour attackColour) {
			for (Move move : Move.possibleMovesTo(m.to)) {
				ColouredPiece piece = pieces.get(move.from);
				if (
						piece != null &&
						piece.colour == attackColour &&
						move.isPossibleFor(piece) &&
						occupied.disjoint(move.intermediateSquares, m.from)
				) return true;
			}
			return false;
		}

	}
	
}
