package com.tomgibara.chess;

import java.util.Collection;

//TODO support transformations
public class Area {

	private static final Area empty = new Area(Squares.empty(), null);
	
	private static final Area entire = new Area(null, Rectangle.entire());
	
	public static Area empty() {
		return empty;
	}
	
	public static Area entire() {
		return entire;
	}
	
	static Area squares(Collection<? extends Square> squares) {
		if (squares == null) throw new IllegalArgumentException("null squares");
		if (squares.isEmpty()) return empty;
		Squares ips = Squares.immutable(squares);
		Rectangle bounds = Rectangle.bound(ips);
		return new Area( bounds.squareArea() == ips.size() ? null : ips, bounds);
	}
	
	static Area rectangle(Rectangle rectangle) {
		if (rectangle == null) throw new IllegalArgumentException("null rectangle");
		return new Area(null, rectangle);
	}

	// null if empty
	private final Rectangle bounds;
	// null if a rectangle
	private final Squares squares;
	
	private Area(Squares squares, Rectangle bounds) {
		this.squares = squares;
		this.bounds = bounds;
	}
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	public Squares getSquares() {
		return squares == null ? bounds.getSquares() : squares;
	}
	
	public boolean isRectangular() {
		return squares == null;
	}
	
	public boolean isEmpty() {
		return bounds == null;
	}
	
	public boolean isEntire() {
		return squares == null && bounds.isEntire();
	}
	
	public BoardArea on(Board board) {
		if (board == null) throw new IllegalArgumentException("null board");
		return new BoardArea(board, this);
	}
	
	public Area intersect(Area that) {
		//TODO can we strength to == empty?
		if (this.isEmpty() || that.isEmpty()) return empty;
		if (!this.bounds.intersects(that.bounds)) return empty;
		if (this.isRectangular() && that.isRectangular()) return new Area(null, this.bounds.intersect(that.bounds));
		return squares( this.squares.intersect(that.squares) );
	}
	
	//TODO add union
	
	@Override
	public int hashCode() {
		return getSquares().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Area)) return false;
		Area that = (Area) obj;
		return this.getSquares().equals(that.getSquares());
	}
	
	@Override
	public String toString() {
		return isRectangular() ? bounds.toString() : "Squares " + getSquares();
	}

}
