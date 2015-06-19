package com.tomgibara.chess;

public final class MoveConstraint {

	private static int ordinal(Colour toMove, boolean mayCastleC, boolean mayCastleG) {
		int ordinal = 0;
		if (toMove.black) ordinal |= 1;
		if (mayCastleC)   ordinal |= 2;
		if (mayCastleG)   ordinal |= 4;
		return ordinal;
	}

	private final static Squares[] csqrs = new Squares[8];

	private static void populate(Colour toMove, boolean mayCastleC, boolean mayCastleG) {
		Rank rank = toMove.white ? Rank.RK_1 : Rank.RK_8;
		Squares squares = new MutableSquares();
		if (mayCastleC) squares.add(rank.intersect(File.FL_C));
		if (mayCastleG) squares.add(rank.intersect(File.FL_G));
		int ordinal = ordinal(toMove, mayCastleC, mayCastleG);
		csqrs[ordinal] = Squares.immutable(squares);
	}

	static {
		populate(Colour.WHITE, false, false);
		populate(Colour.BLACK, false, false);
		populate(Colour.WHITE, true,  false);
		populate(Colour.BLACK, true,  false);
		populate(Colour.WHITE, false, true );
		populate(Colour.BLACK, false, true );
		populate(Colour.WHITE, true,  true );
		populate(Colour.BLACK, true,  true );
	}
	
	public final static MoveConstraint defaultWhite = new MoveConstraint(Colour.WHITE, true, true, null);
	public final static MoveConstraint defaultBlack = new MoveConstraint(Colour.BLACK, true, true, null);
	
	public final static MoveConstraint defaultForColour(Colour colour) {
		if (colour == null) throw new IllegalArgumentException("null colour");
		return colour.white ? defaultWhite : defaultBlack;
	}
	
	public final Colour toMove;
	public final Squares castlingSquares;
	public final Square enPassantSqr;
	
	public MoveConstraint(Colour toMove, boolean mayCastleC, boolean mayCastleG, File enPassantFile) {
		if (toMove == null) throw new IllegalArgumentException("null toMove");
		this.toMove = toMove;
		this.enPassantSqr = enPassantFile == null ? null : enPassantFile.intersect(toMove.white ? Rank.RK_6 : Rank.RK_3);
		castlingSquares = csqrs[ ordinal(toMove, mayCastleC, mayCastleG) ];
	}

}
