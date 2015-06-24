package com.tomgibara.chess;

public enum CastlingRights {

	___________,
	_________WC,
	______WG___,
	______WG_WC,
	___BC______,
	___BC____WC,
	___BC_WG___,
	___BC_WG_WC,
	BG_________,
	BG_______WC,
	BG____WG___,
	BG____WG_WC,
	BG_BC______,
	BG_BC____WC,
	BG_BC_WG___,
	BG_BC_WG_WC;
	
	private static final CastlingRights[] VALUES = values();
	
	private static final int WC = 1 << 0;
	private static final int WG = 1 << 1;
	private static final int BC = 1 << 2;
	private static final int BG = 1 << 3;

	private static Squares squares(Colour toMove, boolean mayCastleC, boolean mayCastleG) {
		Rank rank = toMove.white ? Rank.RK_1 : Rank.RK_8;
		Squares squares = new MutableSquares();
		if (mayCastleC) squares.add(rank.intersect(File.FL_C));
		if (mayCastleG) squares.add(rank.intersect(File.FL_G));
		return Squares.immutable(squares);
	}

	public static CastlingRights with(
			boolean whiteMayCastleC,
			boolean whiteMayCastleG,
			boolean blackMayCastleC,
			boolean blackMayCastleG
			) {
		int ordinal = 0;
		if (whiteMayCastleC) ordinal += WC;
		if (whiteMayCastleG) ordinal += WG;
		if (blackMayCastleC) ordinal += BC;
		if (blackMayCastleG) ordinal += BG;
		return VALUES[ordinal];
	}

	public final boolean whiteMayCastleC;
	public final boolean whiteMayCastleG;
	public final boolean blackMayCastleC;
	public final boolean blackMayCastleG;
	public final Squares whiteSquares;
	public final Squares blackSquares;
	
	private CastlingRights() {
		int ordinal = ordinal();
		whiteMayCastleC = (ordinal & WC) != 0;
		whiteMayCastleG = (ordinal & WG) != 0;
		blackMayCastleC = (ordinal & BC) != 0;
		blackMayCastleG = (ordinal & BG) != 0;
		whiteSquares = squares(Colour.WHITE, whiteMayCastleC, whiteMayCastleG);
		blackSquares = squares(Colour.BLACK, blackMayCastleC, blackMayCastleG);
	}
	
	public Squares squaresFor(Colour colour) {
		return colour.white ? whiteSquares : blackSquares;
	}

	//TODO additional file parameter removable when MoveConstraint is full ordinalized
	public MoveConstraint asMoveConstraint(Colour toMove, File enPassantFile) {
		boolean mayCastleC = toMove.white ? whiteMayCastleC : blackMayCastleC;
		boolean mayCastleG = toMove.white ? whiteMayCastleG : blackMayCastleG;
		return new MoveConstraint(toMove, mayCastleC , mayCastleG, enPassantFile);
	}
	
	//TODO should introduce BoardMove?
	public CastlingRights after(Piece piece, Move move) {
		if (this == ___________) return this;
		final int ordinal = ordinal();
		final int nextOrdinal;
		switch (piece.type) {
		case KING: {
			nextOrdinal = piece.colour.white ? ordinal & BG_BC______.ordinal() : ______WG_WC.ordinal();
			break;
		}
		case ROOK: {
			Square from = move.from;
			boolean white = piece.colour.white;
			Rank rank = white ? Rank.RK_1 : Rank.RK_8;
			if (from.rank != rank) return this;
			switch (from.file) {
			case FL_C : nextOrdinal = ordinal & (white ?  BG_BC_WG___.ordinal() : BG____WG_WC.ordinal()); break;
			case FL_G : nextOrdinal = ordinal & (white ?  BG_BC____WC.ordinal() : ___BC_WG_WC.ordinal()); break;
				default : return this;
			}
		}
			default : return this;
		}
		return nextOrdinal == ordinal ? this : VALUES[nextOrdinal];
	}
}
