package com.tomgibara.chess;

import java.util.ArrayList;
import java.util.List;

public class Sequence {

	private final Pieces pieces;
	private final List<Position> positions = new ArrayList<>();
	private int index = 0;

	// used to create a new continuation of a sequence at a specific position
	private Sequence(Sequence that) {
		this.pieces = that.pieces.mutableCopy();
		positions.add( that.position().copy(this) );
	}

	// used to create a new continuation of a sequence after a particular move
	private Sequence(Position position, int code) {
		//TODO assumes supplied position is 'active' in its sequence
		this.pieces = position.sequence.pieces.mutableCopy();
		positions.add( position.copy(this, code) );
	}

	public Sequence() {
		this(Board.initial().pieces, Colour.WHITE);
	}

	Sequence(Pieces pieces, Colour toMove) {
		this(pieces, toMove, CastlingRights.BG_BC_WG_WC, null);
	}

	Sequence(Pieces pieces, Colour toMove, CastlingRights castlingRights, File enPassantFile) {
		this(pieces, toMove, castlingRights, enPassantFile, 0, 0);
	}

	Sequence(Pieces pieces, Colour toMove, CastlingRights castlingRights, File enPassantFile, int moveNumber, int stalemateClock) {
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		if (toMove == null) throw new IllegalArgumentException("null toMove");
		if (castlingRights == null) throw new IllegalArgumentException("null castlingRights");
		if (moveNumber < 0) throw new IllegalArgumentException("negative moveNumber");
		if (stalemateClock < 0) throw new IllegalArgumentException("negative stalemateClock");
		this.pieces = pieces.mutableCopy();
		positions.add( new Position(this, toMove, castlingRights, enPassantFile, moveNumber, stalemateClock) );
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
	
	Board newBoard() {
		return new Board(pieces);
	}

	// position assumed to be active
	Position makeMove(Position position, int code) {
		Position p;
		if (position.isLast()) {
			p = position.copy(this, code);
			positions.add(p);
			index ++; // since position copy will have advanced state of pieces
		} else {
			p = new Sequence(position, code).position();
		}
		return p;
	}
	
	void addPosition(Position position) {
		positions.add(position);
		// index should already 'last'
		index ++;
	}
	
	int index() {
		return index;
	}
	
	void toIndex(int toIndex) {
		if (index == toIndex) return;

		while (index > toIndex) { // work backwards
			positions.get(index--).unapply(pieces);
		}

		while (index < toIndex) { // work forwards
			positions.get(++index).apply(pieces);
		}
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
