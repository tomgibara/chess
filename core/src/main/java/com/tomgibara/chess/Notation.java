package com.tomgibara.chess;

import java.util.regex.Pattern;

public class Notation {

	private static final Pattern SLASH = Pattern.compile("/");
	
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
	
}
