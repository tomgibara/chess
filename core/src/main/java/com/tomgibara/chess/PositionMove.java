package com.tomgibara.chess;


public final class PositionMove {

	static void transform(Position position, Move[] in, PositionMove[] out) {
		int index = 0;
		for (Move move : in) {
			if (move.isPromotion(position)) {
				out[index++] = new PositionMove(position, move, PieceType.QUEEN);
				out[index++] = new PositionMove(position, move, PieceType.ROOK);
				out[index++] = new PositionMove(position, move, PieceType.BISHOP);
				out[index++] = new PositionMove(position, move, PieceType.KNIGHT);
			} else {
				out[index++] = new PositionMove(position, move, null);
			}
		}
	}
	
	public final Position position;
	public final Move move;
	public final PieceType promotion;
	private Piece piece;
	private Piece capturedPiece = Piece.WHITE_KING;
	
	private PositionMove(Position position, Move move, PieceType promotion) {
		this.position = position;
		this.move = move;
		this.promotion = promotion;
	}
	
	public Piece piece() {
		return piece == null ? piece = position.board.pieces.get(move.from) : piece;
	}
	
	public Piece capturedPiece() {
		return capturedPiece == Piece.WHITE_KING ? capturedPiece = position.board.pieces.get(move.to) : capturedPiece;
	}
	
	public boolean isCapture() {
		return capturedPiece() != null;
	}
	
	public boolean isCheck() {
	}
	
	public boolean isPromotion() {
		return promotion != null;
	}
	
	public Position make() {
		//TODO sequence needs to record made moves
		return position.makeMove(this);
	}
	
	@Override
	public int hashCode() {
		return move.hashCode() + position.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof PositionMove)) return false;
		PositionMove that = (PositionMove) obj;
		return
				this.move == that.move &&
				this.promotion != that.promotion &&
				this.position.equals(that.position);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		// note creates a circularity risk
		PositionMove m = position.moves().list().ambiguatingMove(this);
		PieceType type = piece().type;
		if (type == PieceType.PAWN) {
			if (m != null) sb.append(move.from.file);
			sb.append(isCapture() ? 'x' : '-').append(move.to);
			if (isPromotion()) {
				sb.append('=').append(promotion.character);
			}
		} else {
			sb.append(type.character);
			if (m != null) {
				Square s1 = move.from;
				Square s2 = m.move.from;
				if (s1.file != s2.file) {
					sb.append(s1.file.character);
				} else if (s1.rank != s2.rank) {
					sb.append(s1.rank.character);
				} else {
					sb.append(s1);
				}
			}
			sb.append(isCapture() ? 'x' : '-').append(move.to);
		}
		if (isCheck()) sb.append('+');
		return sb.toString();
	}
	
}
