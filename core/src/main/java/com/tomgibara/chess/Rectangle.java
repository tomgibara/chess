package com.tomgibara.chess;

import java.util.function.Consumer;

//TODO could cache single square rectangles
public final class Rectangle {

	private static final Rectangle entire = new Rectangle(File.FL_A, File.FL_H, Rank.RK_1, Rank.RK_8);
	
	static Rectangle square(Square square) {
		if (square == null) throw new IllegalArgumentException("null square");
		return new Rectangle(square.file, square.file, square.rank, square.rank);
	}
	
	static Rectangle file(File file) {
		if (file == null) throw new IllegalArgumentException("null file");
		return new Rectangle(file, file, Rank.RK_1, Rank.RK_8);
	}
	
	static Rectangle rank(Rank rank) {
		if (rank == null) throw new IllegalArgumentException("null rank");
		return new Rectangle(File.FL_A, File.FL_H, rank, rank);
	}
	
	public static Rectangle entire() {
		return entire;
	}

	public static Rectangle from(File left, File right, Rank bottom, Rank top) {
		if (left == null) throw new IllegalArgumentException("null left");
		if (right == null) throw new IllegalArgumentException("null right");
		if (bottom == null) throw new IllegalArgumentException("null bottom");
		if (top == null) throw new IllegalArgumentException("null top");
		if (left.compareTo(right) > 0) throw new IllegalArgumentException("left > right");
		if (bottom.compareTo(top) > 0) throw new IllegalArgumentException("bottom > top");
		return new Rectangle(left, right, bottom, top);
	}
	
	public static Rectangle from(File left, File right) {
		if (left == null) throw new IllegalArgumentException("null left");
		if (right == null) throw new IllegalArgumentException("null right");
		if (left.compareTo(right) > 0) throw new IllegalArgumentException("left > right");
		return new Rectangle(left, right, Rank.RK_1, Rank.RK_8);
	}
	
	public static Rectangle from(Rank bottom, Rank top) {
		if (bottom == null) throw new IllegalArgumentException("null bottom");
		if (top == null) throw new IllegalArgumentException("null top");
		if (bottom.compareTo(top) > 0) throw new IllegalArgumentException("bottom > top");
		return new Rectangle(File.FL_A, File.FL_H, bottom, top);
	}
	
	public static Rectangle bound(Squares squares) {
		if (squares == null) throw new IllegalArgumentException("null squares");
		if (squares.isEmpty()) throw new IllegalArgumentException("empty squares");
		return new Bounder(squares).rectangle();
	}
	
	public final File left;
	public final File right;
	public final Rank bottom;
	public final Rank top;
	
	private Squares squares;
	
	private Rectangle(File left, File right, Rank bottom, Rank top) {
		this.left = left;
		this.right = right;
		this.bottom = bottom;
		this.top = top;
		squares = null;
	}

	private Rectangle(File left, File right, Rank bottom, Rank top, Squares squares) {
		this(left, right, bottom, top);
		this.squares = squares;
	}

	public boolean isSquare() {
		return left == right && bottom == top;
	}
	
	public Square asSquare() {
		return isSquare() ? Square.at(left, bottom) : null;
	}
	
	public boolean isFile() {
		return left == right && bottom == Rank.RK_1 && top == Rank.RK_8;
	}
	
	public File asFile() {
		return isFile() ? left : null;
	}
	
	public boolean isRank() {
		return bottom == top && left == File.FL_A && right == File.FL_H;
	}
	
	public Rank asRank() {
		return isRank() ? bottom : null;
	}
	
	public boolean isEntire() {
		return left == File.FL_A && right == File.FL_H && bottom == Rank.RK_1 && top == Rank.RK_8;
	}
	
	public boolean intersects(Rectangle that) {
		if (that == null) throw new IllegalArgumentException("null that");
		return
				this.left.ordinal()   <= that.right.ordinal() &&
				this.right.ordinal()  >= that.left.ordinal()  &&
				this.bottom.ordinal() <= that.top.ordinal()   &&
				this.top.ordinal()    >= that.bottom.ordinal() ;
	}
	
	public Rectangle intersect(Rectangle that) {
		if (this.isEntire()) return that;
		if (that.isEntire()) return this;
		if (!intersects(that)) throw new IllegalArgumentException("empty intersection");
		return new Rectangle(
				File.max(this.left,   that.left   ),
				File.min(this.right,  that.right  ),
				Rank.max(this.bottom, that.bottom ),
				Rank.min(this.top,    that.top    )
				);
	}
	
	public int squareArea() {
		return (1 + right.difference(left)) * (1 + top.difference(bottom));
	}
	
	public Squares getSquares() {
		if (squares == null) {
			if (left == right && top == bottom) {
				squares = Squares.singleton(Square.at(left, bottom));
			} else {
				MutableSquares set = new MutableSquares();
				for (int rank = bottom.ordinal(); rank <= top.ordinal(); rank++) {
					for (int file = left.ordinal(); file <= right.ordinal(); file++) {
						set.add(Square.at(file, rank));
					}
				}
				squares = Squares.immutable(set);
			}
		}
		return squares; 
	}
	
	public Area asArea() {
		return Area.rectangle(this);
	}
	
	// convenience method
	public BoardArea on(Board board) {
		return asArea().on(board);
	}
	
	@Override
	public int hashCode() {
		return
				left.hashCode() 
				+ 31 * (right.hashCode()
						+ 31 * (top.hashCode()
								+ 31 * bottom.hashCode()));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Rectangle)) return false;
		Rectangle that = (Rectangle) obj;
		if (this.left   != that.left  ) return false;
		if (this.right  != that.right ) return false;
		if (this.bottom != that.bottom) return false;
		if (this.top    != that.top   ) return false;
		return true;
	}
	
	
	@Override
	public String toString() {
		if (isEntire()) return "Entirety";
		if (isFile()) return "File " + asFile();
		if (isRank()) return "Rank " + asRank();
		if (isSquare()) return "Square " + asSquare();
		return left + "-" + right + "x" + bottom + "-" + top;
	}

	private static class Bounder implements Consumer<Square> {

		private final Squares squares;
		
		File l = File.FL_H;
		File r = File.FL_A;
		Rank b = Rank.RK_8;
		Rank t = Rank.RK_1;
		
		Bounder(Squares squares) {
			this.squares = squares;
			squares.forEach(this);
		}
		
		@Override
		public void accept(Square p) {
			File f = p.file;
			Rank k = p.rank;
			if (f.compareTo(l) < 0) l = f;
			if (f.compareTo(r) > 0) r = f;
			if (k.compareTo(b) < 0) b = k;
			if (k.compareTo(t) > 0) t = k;
		}
		
		Rectangle rectangle() {
			int area = (1 + r.difference(l)) * (1 + t.difference(b));
			return new Rectangle(l, r, b, t, area == squares.size() ? Squares.immutable(squares): null);
		}

	}
	
	
	
}
