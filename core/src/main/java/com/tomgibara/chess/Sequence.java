package com.tomgibara.chess;

import java.util.ArrayList;
import java.util.List;

//TODO look to implement positional locking with mutable a mutable underlying board
public class Sequence {

	private final List<Position> positions = new ArrayList<>();
	private int index = 0;

	private Sequence(Sequence that) {
		positions.add( that.position().copy(this) );
	}
	
	public Sequence() {
		this(Board.initial(), Colour.WHITE);
	}

	public Sequence(Board board, Colour toMove) {
		this(board, toMove, CastlingRights.BG_BC_WG_WC, null);
	}

	public Sequence(Board board, Colour toMove, CastlingRights castlingRights, File enPassantFile) {
		this(board, toMove, castlingRights, enPassantFile, 0, 0);
	}

	public Sequence(Board board, Colour toMove, CastlingRights castlingRights, File enPassantFile, int moveNumber, int stalemateClock) {
		if (board == null) throw new IllegalArgumentException("null board");
		if (toMove == null) throw new IllegalArgumentException("null toMove");
		if (castlingRights == null) throw new IllegalArgumentException("null castlingRights");
		if (moveNumber < 0) throw new IllegalArgumentException("negative moveNumber");
		if (stalemateClock < 0) throw new IllegalArgumentException("negative stalemateClock");
		positions.add( new Position(this, board, toMove, castlingRights, enPassantFile, moveNumber, stalemateClock) );
	}
	
	public int length() {
		return positions.size();
	}
	
	public Position position() {
		return positions.get(index);
	}
	
	public Position initialPosition() {
		index = 0;
		return position();
	}
	
	public Position finalPosition() {
		index = positions.size() - 1;
		return position();
	}
	
	public Position position(int index) {
		if (index < 0) throw new IllegalArgumentException();
		if (index >= positions.size()) throw new IllegalArgumentException();
		return positions.get(index);
	}
	
	public Sequence newContinuation() {
		return new Sequence(this);
	}
	
	public void setLength(int length) {
		int size = positions.size();
		if (length == size) return;
		if (length > size) throw new IllegalArgumentException();
		
	}
	
	int index() {
		return index;
	}
	
	void toIndex(int index) {
		//TODO in future this will need to iterate through moves
		this.index = index;
	}

	void discard(int fromIndex) {
		int size = positions.size();
		if (fromIndex + 1 == size) {
			Position position = positions.remove(fromIndex);
			position.markAsDiscarded();
		} else {
			List<Position> list = fromIndex == 0 ? positions : positions.subList(fromIndex, size);
			list.forEach(p -> p.markAsDiscarded());
			list.clear();
		}
	}

}
