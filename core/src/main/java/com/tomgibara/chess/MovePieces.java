package com.tomgibara.chess;

public final class MovePieces {

	private static final int TYPE_COUNT = PieceType.COUNT;
	private static final int PROMOS = TYPE_COUNT * 4;
	public static final int COUNT = PROMOS + TYPE_COUNT * TYPE_COUNT;
	private static final MovePieces[] pieces = new MovePieces[COUNT];
	
	static {
		for (int ordinal = 0; ordinal < PROMOS; ordinal++) {
			pieces[ordinal] = new MovePieces(
					ordinal,
					PieceType.PAWN,
					PieceType.valueOf(  ordinal >> 2      ),
					PieceType.valueOf( (ordinal  & 3) + 1 )
					);
		}
		for (int ordinal = 0; ordinal < COUNT - PROMOS; ordinal++) {
			pieces[ordinal] = new MovePieces(
					ordinal + PROMOS,
					PieceType.valueOf(ordinal / 6),
					PieceType.valueOf(ordinal % 6),
					null
					);
		}
	}
	
	public static final MovePieces from(int ordinal) {
		return pieces[ordinal];
	}
	
	public static final MovePieces promotion(PieceType captured, PieceType promotion) {
		if (promotion == PieceType.PAWN || promotion == PieceType.KING) throw new IllegalArgumentException("invalid promotion");
		return pieces[(captured.ordinal() << 2) + promotion.ordinal() - 1];
	}
	
	public static final MovePieces regular(PieceType moved, PieceType captured) {
		return pieces[moved.ordinal() * 6 + captured.hashCode()];
	}

	public final int ordinal;
	public final PieceType moved;
	public final PieceType captured;
	public final PieceType promotion;

	private MovePieces(int ordinal, PieceType moved, PieceType captured, PieceType promotion) {
		this.ordinal = ordinal;
		this.moved = moved;
		this.captured = captured;
		this.promotion = promotion;
	}

	@Override
	public int hashCode() {
		return ordinal;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(moved.character);
		if (captured != null) sb.append('x').append(captured.character);
		if (promotion != null) sb.append('=').append(promotion.character);
		return sb.toString();
	}
}
