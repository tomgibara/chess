package com.tomgibara.chess;

import java.util.regex.Pattern;

public class Notation {

	private static final Pattern SPACE = Pattern.compile(" ");
	private static final Pattern SLASH = Pattern.compile("/");
	private static final Pattern CASTLE = Pattern.compile("[^kqKQ]");
	
	public static Board parseFENBoard(String str) {
		String[] split = SLASH.split(str);
		if (split.length != 8) throw new IllegalArgumentException("Incorrect number of ranks: " + split.length);
		Pieces ps = new Pieces();
		Piece[] pieces = new Piece[8];
		for (Rank rank : Rank.values()) {
			String s = split[7 - rank.ordinal()];
			int len = s.length();
			if (len == 0) throw new IllegalArgumentException("Undescribed rank: " + rank);
			int p = 0;
			for (int i = 0; i < len; i++) {
				char c = s.charAt(i);
				if (Character.isDigit(c)) {
					int n = c - '0';
					if (n > 8) throw new IllegalArgumentException("Too many pieces in a rank");
					for (; n > 0; n--) pieces[p++] = null;
				} else {
					pieces[p++] = Character.isLowerCase(c) ?
							PieceType.valueOf(Character.toUpperCase(c)).black() :
							PieceType.valueOf(c).white();
				}
			}
			ps.set(rank.asArea(), pieces);
		}
		return ps.newBoard();
	}

	public static Position parseFENPosition(String str) {
		return parseFENPosition( SPACE.split(str) );
	}

	public static Position parseFENPosition(String... parts) {
		if (parts.length > 6) throw new IllegalArgumentException("Too many arguments");
		
		// board
		Board board = parseFENBoard(parts[0]);
		
		// colour
		final Colour colour;
		if (parts.length < 2) {
			colour = Colour.WHITE;
		} else {
			String colStr = parts[1];
			if (colStr.length() != 1) throw new IllegalArgumentException("Invalid colour");
			colour = Colour.valueOf(Character.toLowerCase(colStr.charAt(0)));
		}
		
		// castling
		final CastlingRights castlingRights;
		if (parts.length < 3) {
			castlingRights = CastlingRights.BG_BC_WG_WC;
		} else {
			String cstStr = parts[2];
			if (cstStr.equals("-")) {
				castlingRights = CastlingRights.BG_BC_WG_WC;
			} else {
				if (CASTLE.matcher(cstStr).find()) throw new IllegalArgumentException("Invalid castle specifier");
				castlingRights = CastlingRights.with(
						cstStr.indexOf('Q') != -1,
						cstStr.indexOf('K') != -1,
						cstStr.indexOf('q') != -1,
						cstStr.indexOf('k') != -1
						);
			}
		}
		
		// en-passant
		final File enPassantFile;
		if (parts.length < 4) {
			enPassantFile = null;
		} else {
			String epStr = parts[3];
			if (epStr.equals("-")) {
				enPassantFile = null;
			} else {
				enPassantFile = Square.at(epStr).file;
			}
		}
		
		// stalement clock
		final int stalemateClock;
		if (parts.length < 5) {
			stalemateClock = 0;
		} else {
			stalemateClock = Integer.parseInt(parts[4]);
			if (stalemateClock < 0) throw new IllegalArgumentException("negative half-move count");
		}
		
		// move number
		final int moveNumber;
		if (parts.length < 6) {
			moveNumber = 0;
		} else {
			moveNumber = Integer.parseInt(parts[5]);
			if (moveNumber < 0) throw new IllegalArgumentException("negative move number");
		}
		
		// return
		return new Sequence(board, colour, castlingRights, enPassantFile, moveNumber, stalemateClock).position();
	}
	
}
