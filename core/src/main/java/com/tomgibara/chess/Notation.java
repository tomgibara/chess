package com.tomgibara.chess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Notation {

	private static final Pattern SPACE = Pattern.compile(" ");
	private static final Pattern SLASH = Pattern.compile("/");
	private static final Pattern CASTLE = Pattern.compile("[^kqKQ]");
	private static final Pattern WS = Pattern.compile("\\s+");
	private static final Pattern TAG = Pattern.compile("\\[\\s*([a-zA-Z0-9][a-zA-Z0-9_]*)\\s*\\\"(([^\\\\\"]|\\\\\\\\|\\\\\\\")*)\\\"\\s*\\]");
	private static final Pattern NUMBER = Pattern.compile("([1-9][0-9]*)\\.");
	
	private static final String unescapeTag(String str) {
		return str.replace("\\\"", "\"").replace("\\\\", "\\");
	}
	
	public static Pieces parseFENPieces(String str) {
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
		return ps;
	}

	public static Position parseFENPosition(String str) {
		return parseFENPosition( SPACE.split(str) );
	}

	public static Position parseFENPosition(String... parts) {
		if (parts.length > 6) throw new IllegalArgumentException("Too many arguments");
		
		// pieces
		Pieces pieces = parseFENPieces(parts[0]);
		
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
				castlingRights = CastlingRights.___________;
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
		return new Sequence(pieces, colour, castlingRights, enPassantFile, moveNumber, stalemateClock).position();
	}
	
	public static Game parse(Reader reader) throws IOException {
		BufferedReader br = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
		return parse(br);
	}
	
	//TODO doesn't process tags split over lines
	private static Game parse(BufferedReader reader) throws IOException {
		int phase = 0; // 0 - pre, 1 - tags, 2 - number, 3 white move, 4 - black move
		Map<String, String> map = null;
		Sequence sequence = null;
		while (true) {
			String line = reader.readLine();

			// common considerations
			if (line == null) {
				switch (phase) {
				case 0 : throw new IllegalArgumentException("missing tags");
				case 1 : throw new IllegalArgumentException("missing move text");
				case 3 : throw new IllegalArgumentException("missing move");
				default: return new Game(map, sequence);
				}
			}
			if (line.isEmpty()) {
				switch (phase) {
				case 0 :
					continue;
				case 1 :
					//TODO support FEN tag and use for initial position as necessary
					sequence = new Sequence();
					phase = 2;
					continue;
				case 3:
					throw new IllegalStateException();
				default:
					return new Game(map, sequence);
				}
			}
			if (line.charAt(0) == '%') continue;
			if (WS.matcher(line).matches()) continue; //TODO correct?

			// specific processing
			switch (phase) {
			case 0 : { // before tags
				phase = 1;
				map = new HashMap<>();
				/* non-empty line, fall through for processing */
			}
			case 1: {
				Matcher m = TAG.matcher(line);
				int i;
				for (i = 0; m.find(i); i = m.end()) {
					int j = m.start();
					if (i != j && !WS.matcher(line.substring(i, j)).matches()) {
						throw new IllegalArgumentException("non-whitespace between tags: " + line.substring(i, j));
					}
					String tagName = line.substring(m.start(1), m.end(1));
					String tagValue = line.substring(m.start(2), m.end(2));
					map.put(tagName, unescapeTag(tagValue));
				}
				if (i != line.length()) {
					if (!WS.matcher(line.substring(i, line.length())).matches()) {
						throw new IllegalArgumentException("non-whitespace after tags: " + line.substring(i, line.length()));
					}
				}
				break;
			}
			default: {
				//TODO accumulate sequence
				String[] split = WS.split(line.trim());
				for (int i = 0; i < split.length; i++) {
					String str = split[i];
					if (str.equals("0-1") || str.equals("1-0") || str.equals("1/2-1/2") || str.equals("*")) {
						return new Game(map, sequence);
					}
					switch (phase) {
					case 2 : {
						Matcher m = NUMBER.matcher(str);
						if (!m.matches()) throw new IllegalArgumentException("expected move number");
						//TODO how to use move number?
						phase = 3;
						continue;
					}
					case 3 :
					case 4 : {
						sequence.finalPosition().moves().make(str);
						phase = phase == 3 ? 4 : 2;
						continue;
					}
					default: throw new IllegalStateException("phase " + phase);
					}
				}
			}
			}
		}
	}
	
}
