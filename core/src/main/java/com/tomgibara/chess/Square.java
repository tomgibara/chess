package com.tomgibara.chess;

//TODO add hasNext() etc...
public final class Square {

	private static final Square[] squares = new Square[64];
	
	static {
		for (int i = 0; i < 64; i++) {
			squares[i] = new Square(i);
		}

		for (int i = 0; i < 64; i++) {
			Square square = squares[i];
			int rank = i >> 3;
			int file = i & 7;

			square.next     = i < 63 ? squares[i + 1] : square;
			square.previous = i >  0 ? squares[i - 1] : square;

			square.up    = rank < 7 ? squares[i + 8] : square;
			square.down  = rank > 0 ? squares[i - 8] : square;
			square.right = file < 7 ? squares[i + 1] : square;
			square.left  = file > 0 ? squares[i - 1] : square;

			square.mirrored = squares[(rank     << 3) + 7 - file];
			square.flipped  = squares[(7 - rank << 3)     + file];
			square.mirrored = squares[63 - i];
		}
	}
	
	public static Square at(int ordinal) {
		if (ordinal < 0 || ordinal > 63) throw new IllegalArgumentException("invalid ordinal");
		return squares[ordinal];
	}

	public static Square at(String str) {
		if (str == null) throw new IllegalArgumentException("null str");
		if (str.isEmpty()) throw new IllegalArgumentException("empty str");
		if (str.length() != 2) throw new IllegalArgumentException("invalid str");
		File file = File.valueOf( str.charAt(0) );
		Rank rank = Rank.valueOf( str.charAt(1) );
		return at(file, rank);
	}

	public static Square at(int file, int rank) {
		if (file < 0 || file >= 8) throw new IllegalArgumentException("invalid file");
		if (rank < 0 || rank >= 8) throw new IllegalArgumentException("invalid rank");
		return squares[(rank << 3) + file];
	}
	
	public static Square at(File file, Rank rank) {
		if (file == null) throw new IllegalArgumentException("null file");
		if (rank == null) throw new IllegalArgumentException("null rank");
		return squares[ (rank.ordinal() <<3) + file.ordinal() ];
	}
	
	public final File file;
	public final Rank rank;
	public final int ordinal;
	public final boolean light;
	public final boolean dark;
	//TODO see where this can be used
	final long mask;
	private Square next;
	private Square previous;
	private Square up;
	private Square down;
	private Square left;
	private Square right;
	private Square mirrored;
	private Square flipped;
	private Square rotated;
	private Rectangle rectangle;
	private final String string;
	
	private Square(int ordinal) {
		this.ordinal = ordinal;
		file = File.valueOf(ordinal & 7);
		rank = Rank.valueOf(ordinal >> 3);
		int sum = file.ordinal() + rank.ordinal();
		light = (sum & 1) == 1;
		dark = !light;
		mask = 1L << ordinal;
		string = this.file.toString() + this.rank.toString();
	}
	
	public Square getNext() {
		return next;
	}
	
	public Square getPrevious() {
		return previous;
	}
	
	public Square getUp() {
		return up;
	}
	
	public Square getDown() {
		return down;
	}
	
	public Square getLeft() {
		return left;
	}
	
	public Square getRight() {
		return right;
	}
	
	public Square getMirrored() {
		return mirrored;
	}
	
	public Square getFlipped() {
		return flipped;
	}
	
	public Square getRotated() {
		return rotated;
	}
	
	public Square toFile(File file) {
		if (file == null) throw new IllegalArgumentException("null file");
		return file == this.file ? this : squares[ordinal + file.difference(this.file)];
	}
	
	public Square toRank(Rank rank) {
		if (rank == null) throw new IllegalArgumentException("null rank");
		return rank == this.rank ? this : squares[ordinal + (rank.difference(this.rank) << 3)];
	}
	
	public Rectangle asRectangle() {
		if (rectangle == null) {
			rectangle = Rectangle.square(this);
		}
		return rectangle;
	}
	
	public Move to(Square to) {
		return Move.between(this, to);
	}
	
	public Move from(Square from) {
		return Move.between(from, this);
	}
	
	// convenience method
	public BoardArea on(Board board) {
		return asRectangle().asArea().on(board);
	}

	@Override
	public String toString() {
		return string;
	}
}
