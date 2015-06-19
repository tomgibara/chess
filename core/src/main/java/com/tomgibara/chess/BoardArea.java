package com.tomgibara.chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO could contrive to preserve some derived fields when updating?
//TODO eliminate class?
public final class BoardArea {

	public final Board board;
	public final Area area;
	
	BoardArea(Board board, Area area) {
		this.board = board;
		this.area = area;
	}
	
	public ColouredPiece getPiece() {
		Square square = areaAsSquare();
		if (square == null) throw new IllegalStateException("area not a square");
		return board.pieces.get(square);
	}
	
//	public BoardArea getOccupiedArea() {
//		Area intersection = board.pieces.keySet().intersect(area);
//		return intersection == area ? this : new BoardArea(board, intersection);
//	}
//	
	public BoardMoves availableMoves(MoveConstraint constraint) {
		if (constraint == null) throw new IllegalArgumentException("null constraint");
		return new BoardMoves(board, area, constraint);
	}
	
	//TODO consider a move/boardmove/movement object?
	public BoardArea movePieceTo(Square toSquare) {
		if (toSquare == null) throw new IllegalArgumentException("null toSquare");
		Square fromSquare = areaAsSquare();
		if (fromSquare == null) throw new IllegalStateException("area not a square");
		return movePiece(fromSquare, toSquare);
	}
	
	public BoardArea movePieceFrom(Square fromSquare) {
		if (fromSquare == null) throw new IllegalArgumentException("null fromSquare");
		Square toSquare = areaAsSquare();
		if (toSquare == null) throw new IllegalStateException("area not a square");
		return movePiece(fromSquare, toSquare);
	}
	
	public BoardArea put(ColouredPiece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		Square square = area.getSquares().first();
		if (piece == board.pieces.get(square)) return this;
		return board.newArrangement().set(square, piece).toBoard().area(area);
		
	}
	
	public BoardArea put(ColouredPiece... pieces) {
		if (pieces == null) throw new IllegalArgumentException("null pieces");
		if (pieces.length == 0) return this;
		//TODO check for nulls
		return board.newArrangement().set(area.getSquares(), pieces).toBoard().area(area);
		
	}
	
	public BoardArea fill(ColouredPiece piece) {
		if (piece == null) throw new IllegalArgumentException("null piece");
		//TODO could try to detect unchanged board
		return board.newArrangement().fill(area.getSquares(), piece).toBoard().area(area);
		
	}
	
	public BoardArea take() {
		return getPiece() == null ? this : board.newArrangement().fill(area.getSquares(), null).toBoard().area(area);
	}
	
	@Override
	public int hashCode() {
		return board.hashCode() + area.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof BoardArea)) return false;
		BoardArea that = (BoardArea) obj;
		return this.board == that.board && this.area.equals(that.area);
	}
	
	@Override
	public String toString() {
		//TODO introduce FEN notation
		return area.toString() + " on board";
	}
	
	private Square areaAsSquare() {
		return area.isRectangular() ? area.getBounds().asSquare() : null;
	}

	//TODO create MoveAnalyzer
	private BoardArea movePiece(Square fromSquare, Square toSquare) {
		if (toSquare == fromSquare) return this;
		ColouredPiece piece = getPiece();
		if (piece == null) return this;
		return board.newArrangement()
				.set(fromSquare, null)
				.set(toSquare, piece)
				.toBoard()
				.area(area);
	}
	
	
}
