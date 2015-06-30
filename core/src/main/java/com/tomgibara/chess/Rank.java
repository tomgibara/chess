package com.tomgibara.chess;

public enum Rank {

	RK_1,
	RK_2,
	RK_3,
	RK_4,
	RK_5,
	RK_6,
	RK_7,
	RK_8;
	
	private final static Rank[] values = values();
	
	public static Rank valueOf(int index) {
		try {
			return values[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static Rank valueOf(char c) {
		return valueOf(c - '1');
	}
	
	public static Rank min(Rank a, Rank b) {
		return a.ordinal() <= b.ordinal() ? a : b;
	}
	
	public static Rank max(Rank a, Rank b) {
		return a.ordinal() >= b.ordinal() ? a : b;
	}
	
	public static int distance(Rank a, Rank b) {
		if (a == null) throw new IllegalArgumentException("null a");
		if (b == null) throw new IllegalArgumentException("null b");
		return Math.abs( a.ordinal() - b.ordinal() );
	}
	
	public static Rank enPassantCaptureRank(Colour toMove) {
		return toMove.white ? Rank.RK_6 : Rank.RK_3;
	}
	
	public static Rank enPassantMoveRank(Colour toMove) {
		return toMove.white ? Rank.RK_5 : Rank.RK_4;
	}
	
	public static Rank castleRank(Colour toMove) {
		return toMove.white ? Rank.RK_1 : Rank.RK_8;
	}
	
	public final char character;
	private final String string;
	private Rectangle rectangle;
	private Area area;
	
	private Rank() {
		character = (char) ('1' + ordinal());
		string = Character.toString(character);
	}
	
	public Rectangle asRectangle() {
		if (rectangle == null) {
			rectangle = Rectangle.rank(this);
		}
		return rectangle;
	}
	
	public Area asArea() {
		if (area == null) {
			area = asRectangle().asArea();
		}
		return area;
	}
	
	public Square intersect(File file) {
		return Square.at(file, this);
	}

	//TODO could cache next/previous?
	public Rank towards(Rank rank) {
		if (rank == this) return this;
		int ordinal = this.ordinal();
		ordinal = ordinal < rank.ordinal() ? ordinal + 1 : ordinal - 1;
		return Rank.valueOf(ordinal);
	}

	public int difference(Rank that) {
		if (that == null) throw new IllegalArgumentException("null that");
		return this.ordinal() - that.ordinal();
	}
	
	@Override
	public String toString() {
		return string;
	}
}
