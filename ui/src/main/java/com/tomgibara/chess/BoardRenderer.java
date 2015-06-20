package com.tomgibara.chess;

import static java.awt.geom.AffineTransform.getScaleInstance;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

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
				Piece piece = board.pieces.get(sqr);
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

	public void render(BoardMoves moves) {
		Colour colour = moves.constraint.toMove;
		SquareMap<List<Move>> map = moves.movesByOriginSquare();
		g.setStroke(new BasicStroke(0.007f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		//fill = translucent(fill, 0.75f);
		for (Entry<Square,List<Move>> entry : map.entrySet()) {
//			MutableSquares visited = new MutableSquares();
//			for (Move move : entry.getValue()) {
//				Squares previous = move.intermediateSquares.intersect(visited);
//				Shape arrow = arrow(move, previous);
			
			List<Move> list = entry.getValue();
			MutableSquares visited = new MutableSquares();
			for (ListIterator<Move> i = list.listIterator(list.size()); i.hasPrevious();) {
				Move move = i.previous();
				if (visited.contains(move.to)) continue;
				Squares occupied = moves.board.pieces.keySet();
				Color fill;
				Color stroke;
				if (occupied.contains(move.to)) {
					fill = Color.RED;
					stroke = null;
				} else {
					fill = colour.white ? Color.WHITE : Color.BLACK;
					stroke = colour.white ? Color.BLACK : Color.WHITE;
				}
				Shape arrow = arrow(move, null);
				if (fill != null) {
					g.setColor(fill);
					g.fill(arrow);
				}
				if (stroke != null) {
					g.setColor(stroke);
					g.draw(arrow);
				}
				visited.addAll(move.intermediateSquares);
			}
		}
	}
	
	private static Color translucent(Color c, float alpha) {
		int a = (int) (alpha * 255) << 24;
		return new Color(c.getRGB() & 0x00ffffff | a, true);
	}
	
	private static Point2D.Double center(Square square) {
		return new Point2D.Double((square.file.ordinal() + 0.5) / 8.0, (7.0 - square.rank.ordinal() + 0.5) / 8.0);
	}
	
	private Shape arrow(Move move, Squares previous) {
		Point2D.Double base = center(move.from);
		Point2D.Double point = center(move.to);
		double length = base.distance(point) * 8.0;
		Path2D.Double path = new Path2D.Double();
//		if (move.isPossibleFor(PieceType.KNIGHT)) {
//			path.moveTo ( 0.00,  0.50 );
//			path.lineTo ( 0.05,  0.50 ); path.curveTo( 0.05,  1.00, x2, y2, 2.40, 1.00);
//			path.lineTo ( 2.40,  1.00 );
//			path.lineTo ( 2.35,  1.00 );
//			path.lineTo ( 2.50,  1.50 );
//			path.lineTo ( 2.65,  1.00 );
//			path.curveTo( 1.00 * length - 0.50, -0.05 - 0.02 * length );
//			path.lineTo ( 0.50                , -0.05                 );
//			path.closePath();
//		} else {
			Square avoid = previous == null ? null : previous.squareFurthestFrom(move.from);
			double except = avoid == null ? 0.0 : center(move.from).distance(center(avoid)) * 8.0 - 0.25;
			path.moveTo( except + 0.50                ,  0.00                 );
			path.lineTo( except + 0.50                ,  0.05 + 0.02 * except );
			path.lineTo(          1.00 * length - 0.50,  0.05 + 0.02 * length );
			path.lineTo(          1.00 * length - 0.45,  0.20                 );
			path.lineTo(          1.00 * length,         0.00                 );
			path.lineTo(          1.00 * length - 0.45, -0.20                 );
			path.lineTo(          1.00 * length - 0.50, -0.05 - 0.02 * length );
			path.lineTo( except + 0.50                , -0.05 - 0.02 * except );
			path.closePath();
//		}
		return transform(path, length, base, point);
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

	private static Shape transform(Shape s, double length, Point2D.Double a, Point2D.Double b) {
		return transFrom(length, a, b).createTransformedShape(s);
	}
	
	private static AffineTransform transFrom(double length, Point2D.Double a, Point2D.Double b) {
//		return transFrom(new Point2D.Double(0.0, 0.0), new Point2D.Double(1.0, 0), a, b);
		double s = a.distance(b);
		double x = (b.x - a.x) / s;
		double y = (b.y - a.y) / s;
		double t = Math.atan2(y, x);
		AffineTransform tr = new AffineTransform();
		tr.translate(a.x, a.y);
		tr.scale(s / length, s / length);
		tr.rotate(x, y);
		return tr;
	}
	
//	private static AffineTransform transFrom(Point2D.Double a1, Point2D.Double b1, Point2D.Double a2, Point2D.Double b2) {
//		double s = a2.distance(b2) / a1.distance(b1);
//		double tx = a1.
//	}
	
}
