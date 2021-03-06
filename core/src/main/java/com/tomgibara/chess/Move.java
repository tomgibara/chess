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
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.tomgibara.storage.AbstractStore;

public final class Move implements Comparable<Move> {

	private static final int WHITE_PAWN     = 0b0000000001;
	private static final int BLACK_PAWN     = 0b0000000010;
	private static final int PAWN_CAPTURE   = 0b0000000100;
	private static final int PROMOTION      = 0b0000001000;
	private static final int KNIGHT         = 0b0000010000;
	private static final int BISHOP         = 0b0000100000;
	private static final int ROOK           = 0b0001000000;
	private static final int KING           = 0b0010000000;
	private static final int WHITE_CASTLE   = 0b0100000000;
	private static final int BLACK_CASTLE   = 0b1000000000;
	
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
	
	private static final Map<PieceType, List<Move>> typeMoves;

	private static final MoveList[] movesFrom = new MoveList[64];
	private static final MoveList[] movesTo = new MoveList[64];

	static {
		possibleMoves = allMoves.stream().filter( m -> m.isPossible() ).collect(moveCollector);
		EnumMap<PieceType, List<Move>> map = new EnumMap<PieceType, List<Move>>(PieceType.class);
		map.entrySet().stream().forEach(e -> e.setValue(new ArrayList<Move>()));
		for (PieceType type : PieceType.values()) {
			List<Move> list = allMoves.stream().filter( m -> m.isPossibleFor(type) ).collect(moveCollector);
			map.put(type, list);
		}
		typeMoves = Collections.unmodifiableMap(map);
		for (int i = 0; i < movesFrom.length; i++) {
			movesFrom[i] = new MoveList(i, false);
			movesTo[i] = new MoveList(i, true);
		}
	}
	
	private static int recordMove(int[] moves, int count, Move move, Piece moved, Piece captured) {
		PieceType movedType = moved.type;
		PieceType capturedType = captured == null ? null : captured.type;
		if (movedType == PieceType.PAWN && move.isPromotion()) {
			moves[count++] = PositionMoves.code(move, MovePieces.promotion(capturedType, PieceType.KNIGHT));
			moves[count++] = PositionMoves.code(move, MovePieces.promotion(capturedType, PieceType.BISHOP));
			moves[count++] = PositionMoves.code(move, MovePieces.promotion(capturedType, PieceType.ROOK));
			moves[count++] = PositionMoves.code(move, MovePieces.promotion(capturedType, PieceType.QUEEN));
		} else {
			moves[count++] = PositionMoves.code(move, MovePieces.regular(movedType, capturedType));
		}
		return count;
	}
	
	public static List<Move> allMoves() {
		return allMoves;
	}
	
	public static List<Move> possibleMoves() {
		return possibleMoves;
	}
	
	//TODO use ordered sets?
	public static List<Move> possibleMovesFor(PieceType type) {
		if (type == null) throw new IllegalArgumentException("null type");
		return typeMoves.get(type);
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
	
	private static final SquareMap<Move> emptyMap = newSquareMap().immutable();
	
	public static SquareMap<Move> emptyMap() { return emptyMap; }
	
	public static SquareMap<Move> newSquareMap() {
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
			boolean promotion =
					whitePawn && to.rank == Rank.RK_8 ||
					blackPawn && to.rank == Rank.RK_1;
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
			if (promotion)   flags |= PROMOTION;
			if (knight)      flags |= KNIGHT;
			if (bishop)      flags |= BISHOP;
			if (rook)        flags |= ROOK;
			if (king)        flags |= KING;
			if (whiteCastle) flags |= WHITE_CASTLE;
			if (blackCastle) flags |= BLACK_CASTLE;
			
			if (!king && !knight) { // never intermediate
				MutableSquares squares = new MutableSquares();
				File file = from.file;
				Rank rank = from.rank;
				File toFile = to.file;
				Rank toRank = to.rank;
				while (true) {
					file = file.towards(toFile);
					rank = rank.towards(toRank);
					if (file == toFile && rank == toRank) break;
					squares.add(file.intersect(rank));
				}
				inter = Squares.immutable(squares);
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
	
	public boolean isPromotion() {
		return anySet(PROMOTION);
	}
	
	public boolean isPossibleFor(PieceType type) {
//TODO analyze surprising impact on performance
//		if (piece == null) throw new IllegalArgumentException("null piece");
		switch (type) {
		case BISHOP: return anySet(BISHOP);
		case KING: return anySet(EITHER_KING);
		case KNIGHT: return anySet(KNIGHT);
		case PAWN: return anySet(EITHER_PAWN);
		case QUEEN: return anySet(QUEEN);
		case ROOK: return anySet(ROOK);
		default: return false;
		}
	}
	
	public boolean isPossibleFor(Piece piece) {
//TODO analyze surprising impact on performance
//		if (piece == null) throw new IllegalArgumentException("null piece");
		switch (piece) {
		case BLACK_PAWN : return anySet(BLACK_PAWN);
		case WHITE_PAWN : return anySet(WHITE_PAWN);
		case BLACK_KING : return anySet(BLACK_KING);
		case WHITE_KING : return anySet(WHITE_KING);
		default: return isPossibleFor(piece.type);
		}
	}
	
	public PieceTypes possiblePieces() {
		return PieceTypes.containing(
				isPossibleFor(PieceType.PAWN  ),
				isPossibleFor(PieceType.KNIGHT),
				isPossibleFor(PieceType.BISHOP),
				isPossibleFor(PieceType.ROOK  ),
				isPossibleFor(PieceType.QUEEN ),
				isPossibleFor(PieceType.KING  )
				);
	}
	
	public boolean isPossible() {
		return flags != 0;
	}
	
	public Move reverse() {
		return reverse;
	}
	
	public Square enPassantSquare() {
		return Square.at(to.file, from.rank);
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
	public int compareTo(Move that) {
		return this.ordinal - that.ordinal;
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
		private final long[] masks;
		
		
		MoveList(int ordinal, boolean reverse) {
			this.ordinal = ordinal;
			this.reverse = reverse;
			square = Square.at(ordinal);

			long bits = 0L;
			long bit = 1L;
			int size = 0;
			long[] masks = new long[Piece.COUNT];
			Piece[] pieces = Piece.values();
			for (int offset = 0; offset < 64; offset++) {
				int p = ptr(offset);
				Move move = moves[p];
				if (move.isPossible()) {
					bits |= bit;
					size ++;
				}
				for (int i = 0; i < pieces.length; i++) {
					if (move.isPossibleFor(pieces[i])) {
						masks[i] |= bit;
					}
				}
				bit <<= 1;
			}
			this.bits = bits;
			this.size = size;
			this.masks = masks;

			map = new SquareMap<Move>(new AbstractStore<Move>() {

				@Override
				public Class<Move> valueType() { return Move.class; }

				@Override
				public int count() { return MoveList.this.size; }

				@Override
				public Move get(int index) {
					Move move = moves[ptr(index)];
					return move.isPossible() ? move : null;
				}

				@Override
				public int size() {
					return 64;
				}

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
		
		@Override
		public void forEach(Consumer<? super Move> action) {
			//TODO use smarter iteration?	
			for (int offset = 0; offset < 64; offset++) {
				Move move = moves[ptr(offset)];
				if (move.isPossible()) {
					action.accept(move);
				}
			}
		}
		
		//TODO would like to eliminate branch - subclass instead?
		private int ptr(int off) {
			return reverse ? (off << 6) + ordinal : (ordinal << 6) + off;
		}
		
		private int off(int ptr) {
			return reverse ? (ptr >> 6) & 0x3f : ptr & 0x3f;
		}

		int populateMoves(int[] moveCodes, int moveCount, Board board, MoveConstraint constraint, Squares checkers, Squares interpose) {
			SquareMap<Piece> pieces = board.pieces;
			Piece piece = pieces.get(square);
			Squares occupied = pieces.keySet();
			Colour colour = constraint.toMove;
			Squares occupiedBySameColour = board.withColour(colour).occupiedSquares();

			if (checkers.isEmpty() || piece.type == PieceType.KING) {
				// ugly but localized and faster than regular iteration
				long bits = masks[piece.ordinal()] & ~occupiedBySameColour.mask();
				for (int offset = 0; offset < 64 && bits != 0L; offset++, bits >>>= 1) {
					if ((bits & 1L) == 0L) continue; // invalid move
					Move move = Move.moves[ptr(offset)];
					if (!move.isPossibleFor(piece)) continue; // piece cannot move in that way
					if (piece.type == PieceType.PAWN && move.isPawnCapture() == (move.to != constraint.enPassantSqr && !occupied.contains(move.to))) continue; // pawns must capture to move diagonally
					if (occupied.intersects(move.intermediateSquares)) continue; // one or more pieces interposed
					if (piece.type == PieceType.KING) {
						Squares occupiedByOpposingColour = board.withColour(colour.opposite()).occupiedSquares();
						if (possibleMovesTo(move.to).attacks(pieces, occupied, move.from, occupiedByOpposingColour)) continue; // king cannot move into check
						if (move.isCastling()) {
							if (!constraint.castlingSquares.contains(move.to)) continue; // cannot castle invalidly
							if (!checkers.isEmpty()) continue; // cannot castle out of check
							Move rookMove = move.inducedRookMove();
							if (pieces.get(rookMove.from) != PieceType.ROOK.coloured(constraint.toMove)) continue; // rook must be present
							if (occupied.intersects( rookMove.intermediateSquares )) continue; // path must be clear for rook
							if (possibleMovesTo(move.intermediateSquares.only()).attacks(pieces, occupied, rookMove.from, occupiedByOpposingColour)) continue; // cannot pass through check
						}
					} else {
						Interposition pin = board.withColour(piece.colour).pinnedToKing().get(square);
						if (pin != null && !pin.move.spannedSquares.contains(move.to)) continue; // piece breaks pin on king
					}
					moveCount = recordMove(moveCodes, moveCount, move, piece, pieces.get(move.to));
				}
			} else { // check
				// we can try to interpose
				for (Square i : interpose) {
					Move move = Move.between(square, i);
					if (
							move.isPossibleFor(piece) &&
							!occupied.intersects(move.intermediateSquares) &&
							(piece.type != PieceType.PAWN || !move.isPawnCapture() || move.to == constraint.enPassantSqr) && // extra check for en-passant
							board.withColour(piece.colour).pinnedToKing().get(square) == null // extra check for breaking pin
							) {
						moveCount = recordMove(moveCodes, moveCount, move, piece, pieces.get(move.to));
					}
				}
				// we can try to capture
				Square c = checkers.only();
				if (c != null) {
					Move move;
					Square ep = constraint.enPassantSqr;
					boolean valid;
					if (
							ep != null &&
							ep.file == c.file &&
							pieces.get(c).type == PieceType.PAWN &&
							enPassantPossible(piece, c)
						)
					{
						move = Move.between(square, ep);
						valid = true;
					} else {
						move = Move.between(square, c);
						valid = move.isPossibleFor(piece) && !occupied.intersects(move.intermediateSquares);
					}
					if (valid) {
						Interposition pin = board.withColour(piece.colour).pinnedToKing().get(square);
						if (pin == null || pin.move.from == c) {
							moveCount = recordMove(moveCodes, moveCount, move, piece, pieces.get(move.to));
						}
					}
				}
			}
			
			return moveCount;
		}
		
		private boolean enPassantPossible(Piece piece, Square target) {
			return
					piece.type == PieceType.PAWN &&
					target.rank == square.rank &&
					square.rank == Rank.enPassantMoveRank(piece.colour) &&
					File.distance(square.file, target.file) == 1;
		}

		// note only sufficient for determining attacks not by enemy king (ie sufficient for check tests)
		// this is because an attack by a king could be a false positive if the king is moving into check
		private boolean attacks(SquareMap<Piece> pieces, Squares occupied, Square vacated, Squares occupiedByOpposingColour) {
			long bits = this.bits & occupiedByOpposingColour.mask();
			for (int offset = 0; offset < 64 && bits != 0L; offset++, bits >>>= 1) {
				if ((bits & 1L) == 0L) continue; // invalid move
				Move move = moves[ptr(offset)];
				if (!move.isPossible()) continue;
				Piece piece = pieces.get(move.from);
				if (!move.isPossibleFor(piece)) continue;
				switch (piece.type) {
				case PAWN:
					if (move.isPawnCapture()) return true;
					break;
				case KING:
					if (!move.isCastling()) return true;
					break;
				default:
					if (occupied.disjoint(move.intermediateSquares, vacated)) return true;
				}
			}
			return false;
		}

	}
	
}
