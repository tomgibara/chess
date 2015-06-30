package com.tomgibara.chess;

import static com.tomgibara.chess.Square.at;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PositionMoves {

	static final int NO_CODE = -1;
	private static final int AMBIGUOUS_CODE = -2;

	private static final int MAX_MOVES = 256;
	private static final int PIECE_BITS = 6;
	private static final int PIECE_MASK = (1 << PIECE_BITS) - 1;
	
	private static final Comparator<? super Move> moveDistComp = (m1, m2) -> m1.spannedSquares.size() - m2.spannedSquares.size();

	private static ThreadLocal<int[]> tmpCodes = new ThreadLocal<int[]>() {
		protected int[] initialValue() {
			return new int[MAX_MOVES];
		}
	};
	
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
		return makeChecked( codeMatching(move) );
	}
	
	Position make(String move) {
		return makeChecked( codeMatching(move.trim()) );
	}

	Position makeChecked(int code) {
		if (code == NO_CODE) throw new IllegalArgumentException("not a legal move");
		if (code == AMBIGUOUS_CODE) throw new IllegalArgumentException("ambiguous move");
		return position.makeMove(code);
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
	
	int codeMatching(Move move) {
		int code = code(move);
		int i = Arrays.binarySearch(codes, code);
		if (i >= 0) return code;
		i = -1 - i;
		if (i >= codes.length) return NO_CODE;
		int candidate = codes[i++];
		if ((candidate & ~PIECE_MASK) != code) return NO_CODE;
		if (i >= codes.length || (codes[i] & ~PIECE_MASK) != code) return candidate;
		return AMBIGUOUS_CODE;
	}
	
	private int[] codesMatching(PieceType moved, Square to, PieceType promotion) {
		int[] tmp = tmpCodes.get();
		int count = 0;
		for (int i = 0; i < codes.length; i++) {
			int code = codes[i];
			if (codeMove(code).to != to) continue;
			MovePieces pieces = codePieces(code);
			if (pieces.moved != moved) continue;
			if (pieces.promotion != promotion) continue;
			tmp[count++] = code;
		}
		return Arrays.copyOf(tmp, count);
	}
	
	int codeMatching(String move) {
		int len = move.length();
		boolean check;
		boolean mate;
		if (move.endsWith("#")) {
			check = true;
			mate = true;
			len -= 1;
		} else if (move.endsWith("++")) {
			check = true;
			mate = true;
			len -= 2;
		} else if (move.endsWith("+")) {
			check = true;
			mate = false;
			len -= 1;
		} else {
			check = false;
			mate = false;
		}
		// this is what we're searching for...
		final int code;
		if ((move.startsWith("0-0") || move.startsWith("O-O")) && len == 3) {
			// castle king side
			Rank rank = Rank.castleRank(position.toMove);
			Move m = Move.between(at(File.FL_E, rank), at(File.FL_G, rank));
			code = codeMatching(m);
		} else if ((move.startsWith("0-0-0") || move.startsWith("O-O-O")) && len == 5) {
			// castle queen side
			Rank rank = Rank.castleRank(position.toMove);
			Move m = Move.between(at(File.FL_E, rank), at(File.FL_C, rank));
			code = codeMatching(m);
		} else {
			if (len < 2) throw new IllegalArgumentException();
			// check for promotion
			PieceType promo;
			if (move.charAt(len - 2) == '=') {
				char pc = move.charAt(len - 1);
				promo = PieceType.valueOf(pc);
				//TODO actually an optional check - move won't be found anyway
				if (!promo.promotion) throw new IllegalArgumentException("invalid promotion");
				len -= 2;
			} else {
				promo = null;
			}
			// target square
			if (len < 2) throw new IllegalArgumentException();
			Square to = at( move.substring(len - 2, len) ); //TODO could avoid string creation
			len -= 2;
			// capture
			boolean capture;
			File f;
			Rank r;
			int[] matches;
			if (len == 0) {
				// pawn move
				capture = false;
				f = null;
				r = null;
				matches = codesMatching(PieceType.PAWN, to, promo);
			} else {
				capture = move.charAt(len - 1) == 'x';
				if (capture) {
					len -= 1;
					if (len == 0) throw new IllegalArgumentException("capture without origin");
				}
				// origin given
				char c = move.charAt(0);
				if (Character.isUpperCase(c)) {
					// piece move or capture
					PieceType moved = PieceType.valueOf(c);
					//TODO - again optional check - this will never match a move
					if (promo != null) throw new IllegalArgumentException("non-pawn promotion");
					// specifier
					switch (len) {
					case 1 : {
						f = null;
						r = null;
						break;
					}
					case 2 : {
						char k = move.charAt(1);
						if (Character.isDigit(k)) {
							f = null;
							r = Rank.valueOf(k);
						} else {
							f = File.valueOf(k);
							r = null;
						}
						break;
					}
					case 3 : {
						f = File.valueOf(move.charAt(1));
						r = Rank.valueOf(move.charAt(2));
					}
					default: throw new IllegalArgumentException("invalid piece specifier");
					}
					// matches
					matches = codesMatching(moved, to, null);
				} else {
					// pawn move or capture
					if (len != 1) throw new IllegalArgumentException("invalid pawn specifier");
					f = File.valueOf(c);
					r = null;
					matches = codesMatching(PieceType.PAWN, to, promo);
				}
			}
			// matching
			switch (matches.length) {
			case 0 :
				code = NO_CODE;
				break;
			case 1 : {
				int match = matches[0];
				Square s = codeMove(match).from;
				if (f != null && s.file != f || r != null && s.rank != r) {
					code = NO_CODE;
				} else {
					code = match;
				}
				break;
			}
			default:
				int tmp = NO_CODE;
				for(int match : matches) {
					Square s = codeMove(match).from;
					if (f != null && s.file != f || r != null && s.rank != r) continue;
					if (tmp != NO_CODE) {
						tmp = AMBIGUOUS_CODE;
						break;
					} else {
						tmp = match;
					}
				}
				code = tmp;
			}
			boolean cp = isCapture(codeMove(code), codePieces(code));
			if (cp && !capture) throw new IllegalArgumentException("move is a capture");
			if (!cp && capture) throw new IllegalArgumentException("move is not a capture");
		}
		//TODO verify check & mate
		if (code == NO_CODE) throw new IllegalArgumentException("not a legal move");
		if (code == AMBIGUOUS_CODE) throw new IllegalArgumentException("ambiguous move");
		return code;
	}
	
	private static class MovePopulator implements Consumer<Square> {

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
