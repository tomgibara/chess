package com.tomgibara.chess;

import java.util.NoSuchElementException;

public class Position {

	public final Sequence sequence;
	private final int index;
	public final Board board;
	public final Colour toMove;
	public final CastlingRights castlingRights;
	public final File enPassantFile;
	public final int moveNumber; // this is the position *after* this move number
	public final int stalemateClock; // reset on captures and pawn moves
	private boolean discarded = false;

	//TODO derive as needed?
	public final MoveConstraint constraint;
	
	private BoardMoves moves;
	
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

	public BoardMoves moves() {
		checkIndex();
		if (moves == null) {
			moves = new BoardMoves(this, Area.entire());
		}
		return moves;
	}
	
	public BoardMoves computeMoves(Area area) {
		checkIndex();
		return new BoardMoves(this, area);
	}
	
	public void discard() {
		if (discarded) return;
		sequence.discard(index);
	}
	
	Position copy(Sequence owner) {
		checkIndex();
		return new Position(sequence, this);
	}
	
	void markAsDiscarded() {
		this.discarded = true;
	}
	
	//TODO toString using notation?
	
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
