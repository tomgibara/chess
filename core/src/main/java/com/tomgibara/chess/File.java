package com.tomgibara.chess;

public enum File {

	FL_A,
	FL_B,
	FL_C,
	FL_D,
	FL_E,
	FL_F,
	FL_G,
	FL_H;
	
	private final static File[] values = values();
	
	public static File valueOf(int index) {
		try {
			return values[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static File valueOf(char c) {
		return valueOf(c - 'a');
	}
	
	public static File min(File a, File b) {
		return a.ordinal() <= b.ordinal() ? a : b;
	}
	
	public static File max(File a, File b) {
		return a.ordinal() >= b.ordinal() ? a : b;
	}
	
	public static int distance(File a, File b) {
		if (a == null) throw new IllegalArgumentException("null a");
		if (b == null) throw new IllegalArgumentException("null b");
		return Math.abs( a.ordinal() - b.ordinal() );
	}
	
	public final char character;
	private final String string;
	private Rectangle rectangle;

	private File() {
		character = (char) ('a' + ordinal());
		string = Character.toString(character);
	}
	
	public Rectangle asRectangle() {
		if (rectangle == null) {
			rectangle = Rectangle.file(this);
		}
		return rectangle;
	}

	public Square intersect(Rank rank) {
		return Square.at(this, rank);
	}
	
	//TODO could cache next/previous?
	public File towards(File file) {
		if (file == this) return this;
		int ordinal = this.ordinal();
		ordinal = ordinal < file.ordinal() ? ordinal + 1 : ordinal - 1;
		return File.valueOf(ordinal);
	}

	// convenience method
	public BoardArea on(Board board) {
		return asRectangle().asArea().on(board);
	}
	
	public int difference(File that) {
		if (that == null) throw new IllegalArgumentException("null that");
		return this.ordinal() - that.ordinal();
	}
	
	public File nearestFlank() {
		return ordinal() < 4 ? FL_A : FL_H;
	}
	
	@Override
	public String toString() {
		return string;
	}

}
