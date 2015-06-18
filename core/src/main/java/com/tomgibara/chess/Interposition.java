package com.tomgibara.chess;

public final class Interposition {

	private static final SquareMap<Interposition> emptyMap = newSquareMap().immutable();
	
	
	public static SquareMap<Interposition> emptySquareMap() {
		return emptyMap;
	}
	
	public static SquareMap<Interposition> newSquareMap() {
		return new SquareMap<>(new Interposition[64], 0);
	}
	
	public final Move move;
	public final Square square;
	
	Interposition(Move move, Square square) {
		this.move = move;
		this.square = square;
	}
	
	public Move getMove() {
		return move;
	}
	
	public Square getSquare() {
		return square;
	}
	
	@Override
	public int hashCode() {
		return move.ordinal << 6 | square.ordinal;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Interposition)) return false;
		Interposition that = (Interposition) obj;
		return this.move == that.move && this.square == that.square;
	}
	
	@Override
	public String toString() {
		return square + " between " + move;
	}
}
