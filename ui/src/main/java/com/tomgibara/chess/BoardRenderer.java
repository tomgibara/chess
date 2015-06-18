package com.tomgibara.chess;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class BoardRenderer {

	private static final AffineTransform identity = new AffineTransform();
	private static final double EIGHTH = 0.125;
	private static final Rectangle2D SQUARE = new Rectangle2D.Double(0, 0, 1, 1);
	private static final BasicStroke STROKE = new BasicStroke(0.025f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Font FONT;
	
	static {
		Font font;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, BoardRenderer.class.getResourceAsStream("Alpha.ttf"));
		} catch (FontFormatException | IOException e) {
			throw new RuntimeException("Failed to load chess font", e);
		}
		FONT = font;
		//FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(0.125f);
		System.out.println(FONT);
	}
	
	private final int size;
	private final double scale;
	private final BufferedImage image;
	private final Graphics2D g;
	
	public BoardRenderer(int size) {
		if (size < 1) throw new IllegalArgumentException("invalid size");
		this.size = size;
		scale = size;
		image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		g = image.createGraphics();
		reset();
		g.setFont(FONT);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void render(Board board) {
		if (board == null) throw new IllegalArgumentException("null board");
		g.setColor(Color.WHITE);
		g.fill(SQUARE);
//		Line2D.Double line = new Line2D.Double();
//		g.setStroke(STROKE);
		g.setColor(Color.BLACK);
		for (int rank = 0; rank < 8; rank++) {
			for (int file = 0; file < 8; file++) {
				Square sqr = Square.at(file, rank);
				String str;
				ColouredPiece piece = sqr.on(board).getPiece();
				if (piece == null) {
					str = sqr.light ? " " : "+";
				} else if (sqr.light) {
					switch (piece) {
					case BLACK_BISHOP: str = "n"; break;
					case BLACK_KING:   str = "l"; break;
					case BLACK_KNIGHT: str = "j"; break;
					case BLACK_PAWN:   str = "o"; break;
					case BLACK_QUEEN:  str = "w"; break;
					case BLACK_ROOK:   str = "t"; break;
					case WHITE_BISHOP: str = "b"; break;
					case WHITE_KING:   str = "k"; break;
					case WHITE_KNIGHT: str = "h"; break;
					case WHITE_PAWN:   str = "p"; break;
					case WHITE_QUEEN:  str = "q"; break;
					case WHITE_ROOK:   str = "r"; break;
					default: throw new IllegalStateException();
					}
				} else {
					switch (piece) {
					case BLACK_BISHOP: str = "N"; break;
					case BLACK_KING:   str = "L"; break;
					case BLACK_KNIGHT: str = "J"; break;
					case BLACK_PAWN:   str = "O"; break;
					case BLACK_QUEEN:  str = "W"; break;
					case BLACK_ROOK:   str = "T"; break;
					case WHITE_BISHOP: str = "B"; break;
					case WHITE_KING:   str = "K"; break;
					case WHITE_KNIGHT: str = "H"; break;
					case WHITE_PAWN:   str = "P"; break;
					case WHITE_QUEEN:  str = "Q"; break;
					case WHITE_ROOK:   str = "R"; break;
					default: throw new IllegalStateException();
					}
				}
				window(file, rank);
//				for (double c = 0.0; c <= 2.0; c += 0.1) {
//					line.x1 = c;
//					line.y1 = 0;
//					line.x2 = 0;
//					line.y2 = c;
//					g.draw(line);
//				}
				g.drawString(str, 0, 1);
				reset();
			}
		}
		//g.drawString("+pPk", 0.5f, 0.5f);
	}
	
	private void reset() {
		g.setTransform(identity);
		g.scale(scale, scale);
		g.setClip(null);
	}
	
	private void window(int file, int rank) {
		g.scale(EIGHTH, EIGHTH);
		g.translate(file, 7 - rank);
		g.clip(SQUARE);
	}
	
}
