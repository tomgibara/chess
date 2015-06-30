package com.tomgibara.chess;

import static com.tomgibara.chess.PositionMoves.NO_CODE;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class Position {

	public final Sequence sequence;
	final int code;
	private final int index;
	public final Colour toMove;
	public final CastlingRights castlingRights;
	public final File enPassantFile;
	public final int moveNumber; // this is the position *after* this move number
	public final int stalemateClock; // reset on captures and pawn moves
	private boolean discarded = false;

	private final Board board;
	//TODO derive as needed?
	public final MoveConstraint constraint;
	
	private PositionMoves moves;
	
	Position(Sequence sequence, Colour toMove, CastlingRights castlingRights, File enPassantFile, int moveNumber, int stalemateClock) {
		this.sequence = sequence;
		this.code = NO_CODE;
		this.index = sequence.length();
		this.board = sequence.newBoard();
		this.toMove = toMove;
		this.castlingRights = castlingRights;
		this.enPassantFile = enPassantFile;
		this.moveNumber = moveNumber;
		this.stalemateClock = stalemateClock;
		constraint = castlingRights.asMoveConstraint(toMove, enPassantFile);
	}
	
	private Position(Sequence sequence, Position that, int code) {
		this.sequence = sequence;
		this.code = code;
		this.index = sequence.length();
		this.board = sequence.newBoard();

		if (code == NO_CODE) {
			this.toMove = that.toMove;
			this.castlingRights = that.castlingRights;
			this.enPassantFile = that.enPassantFile;
			this.moveNumber = that.moveNumber;
			this.stalemateClock = that.stalemateClock;
			this.constraint = that.constraint;
		} else {
			Move move = PositionMoves.codeMove(code);
			MovePieces pieces = PositionMoves.codePieces(code);
			this.toMove = that.toMove.opposite();
			this.castlingRights = that.castlingRights.after(pieces.moved.coloured(that.toMove), move);
			this.enPassantFile = pieces.moved == PieceType.PAWN && !move.intermediateSquares.isEmpty() ? move.from.file : null;
			this.moveNumber = that.toMove.white ? that.moveNumber + 1 : that.moveNumber;
			//TODO increment stalemate clock
			this.stalemateClock = pieces.moved == PieceType.PAWN || pieces.captured != null ? 0 : that.stalemateClock + 1;
			constraint = castlingRights.asMoveConstraint(toMove, enPassantFile);
			// advance pieces so that new position gets the pieces in its state
			board.pieces.make(that.toMove, move, pieces);
		}
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
	
	public Move previousMove() {
		return code == NO_CODE ? null : PositionMoves.codeMove(code);
	}
	
	public MovePieces previousMovePieces() {
		return code == NO_CODE ? null : PositionMoves.codePieces(code);
	}
	
	public Pieces pieces() {
		activate();
		//TODO cache reference?
		return board.pieces.immutable();
	}

	public PositionMoves moves() {
		activate();
		if (moves == null) {
			moves = new PositionMoves(this, board, Area.entire());
		}
		return moves;
	}
	
	public PositionMoves computeMoves(Area area) {
		activate();
		return new PositionMoves(this, board, area);
	}
	
	public Position makeMove(Move move) {
		if (move == null) throw new IllegalArgumentException("null move");
		return moves().make(move);
	}
	
	public Position makeMove(String move) {
		if (move == null) throw new IllegalArgumentException("null move");
		return moves().make(move);
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

	Position copy(Sequence owner, int code) {
		activate();
		return new Position(owner, this, code);
	}
	
	Position copy(Sequence owner) {
		return copy(owner, NO_CODE);
	}
	
	Position makeMove(int code) {
		activate();
		return sequence.makeMove(this, code);
	}
	
	void apply(Pieces pieces) {
		Move m = PositionMoves.codeMove(code);
		MovePieces p = PositionMoves.codePieces(code);
		pieces.make(toMove.opposite(), m, p);
	}
	
	void unapply(Pieces pieces) {
		Move m = PositionMoves.codeMove(code);
		MovePieces p = PositionMoves.codePieces(code);
		pieces.takeBack(toMove.opposite(), m, p);
	}
	
	void markAsDiscarded() {
		this.discarded = true;
	}
	
	void activate() {
		if (discarded) throw new IllegalStateException();
		sequence.toIndex(index);
	}
	
	boolean isLast() {
		return index + 1 == sequence.length();
	}

	private void checkDiscarded() {
		if (discarded) throw new IllegalStateException();
	}
	
}
