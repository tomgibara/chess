package com.tomgibara.chess;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class Position {

	public final Sequence sequence;
	private final int index;
	private final Board board;
	public final Colour toMove;
	public final CastlingRights castlingRights;
	public final File enPassantFile;
	public final int moveNumber; // this is the position *after* this move number
	public final int stalemateClock; // reset on captures and pawn moves
	private boolean discarded = false;

	//TODO derive as needed?
	public final MoveConstraint constraint;
	
	private PositionMoves moves;
	
	Position(Sequence sequence, Board board, Colour toMove, CastlingRights castlingRights, File enPassantFile, int moveNumber, int stalemateClock) {
		this.sequence = sequence;
		this.index = sequence.length();
		this.board = board;
		this.toMove = toMove;
		this.castlingRights = castlingRights;
		this.enPassantFile = enPassantFile;
		this.moveNumber = moveNumber;
		this.stalemateClock = stalemateClock;
		constraint = castlingRights.asMoveConstraint(toMove, enPassantFile);
	}
	
	private Position(Sequence sequence, Position that) {
		this.sequence = sequence;
		this.index = sequence.length();
		this.board = that.board;
		this.toMove = that.toMove;
		this.castlingRights = that.castlingRights;
		this.enPassantFile = that.enPassantFile;
		this.moveNumber = that.moveNumber;
		this.stalemateClock = that.stalemateClock;
		this.constraint = that.constraint;
	}
	
	public Position previous() {
		checkDiscarded();
		try {
			return sequence.position(index - 1);
		} catch (IllegalArgumentException e) {
			throw new NoSuchElementException();
		}
	}

	public Position next() {
		checkDiscarded();
		try {
			return sequence.position(index + 1);
		} catch (IllegalArgumentException e) {
			throw new NoSuchElementException();
		}
	}
	
	public Pieces pieces() {
		checkIndex();
		//TODO cache reference?
		return board.pieces.immutable();
	}

	public PositionMoves moves() {
		checkIndex();
		if (moves == null) {
			moves = new PositionMoves(this, board, Area.entire());
		}
		return moves;
	}
	
	public PositionMoves computeMoves(Area area) {
		checkIndex();
		return new PositionMoves(this, board, area);
	}
	
	public void discard() {
		if (discarded) return;
		sequence.discard(index);
	}
	
	@Override
	public int hashCode() {
		return board.hashCode() + 0x0f70 * toMove.hashCode() + castlingRights.hashCode() + Objects.hashCode(enPassantFile) + 0x7f0000 * stalemateClock;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Position)) return false;
		Position that = (Position) obj;
		
		if (this.toMove != that.toMove) return false;
		if (this.castlingRights != that.castlingRights) return false;
		if (this.enPassantFile != that.enPassantFile) return false;
		if (this.stalemateClock != that.stalemateClock) return false;
		
		return this.board.equals(that.board);
	}
	
	//TODO toString using notation?

	Position copy(Sequence owner) {
		checkIndex();
		return new Position(sequence, this);
	}
	
	void markAsDiscarded() {
		this.discarded = true;
	}
	
	private void checkDiscarded() {
		if (discarded) throw new IllegalStateException();
	}
	
	private void checkIndex() {
		//if (index != sequence.index()) throw new IllegalStateException("Attempt to access position " + index + " when sequence at index " + sequence.index());
		if (discarded) throw new IllegalStateException();
		sequence.toIndex(index);
	}
	
	private boolean isLast() {
		return index + 1 == sequence.length();
	}

}
