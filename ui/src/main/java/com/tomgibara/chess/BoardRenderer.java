package com.tomgibara.chess;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

public class BoardRenderer {

	private static final Font loadFont(String name) {
		try {
			return Font.createFont(Font.TRUETYPE_FONT, BoardRenderer.class.getResourceAsStream(name));
		} catch (FontFormatException | IOException e) {
			throw new RuntimeException("Failed to load chess font", e);
		}
	}
	
	private static final AffineTransform identity = new AffineTransform();
	private static final double EIGHTH = 0.125;
	private static final double BOARD_INSET = 0.1;
	private static final Rectangle2D SQUARE = new Rectangle2D.Double(0, 0, 1, 1);
	private static final BasicStroke ARROW_STROKE = new BasicStroke(0.005f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Font CHESS_FONT = loadFont("Alpha.ttf");
	private static final Font COORD_FONT = loadFont("Lora-Regular.ttf").deriveFont((float) (BOARD_INSET * 0.6));
	private static final Font ANN_FONT = loadFont("FiraMono-Bold.ttf").deriveFont((float) (BOARD_INSET * 0.5));
	
	private final int size;
	private final double scale;
	private final BufferedImage image;
	private final AffineTransform boardTrans = new AffineTransform();
	private final Graphics2D g;
	
	public BoardRenderer(int size, boolean coords) {
		if (size < 1) throw new IllegalArgumentException("invalid size");
		this.size = size;
		scale = size;
		image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		g = image.createGraphics();
		boardTrans.scale(scale, scale);
		renderBackground();
		g.setFont(COORD_FONT);
		if (coords) {
			double s = 1 - 2 * BOARD_INSET;
			boardTrans.scale(s, s);
			boardTrans.translate(BOARD_INSET / s, BOARD_INSET / s);
			renderCoords();
		}
		reset();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}
	
	public BufferedImage getImage() {
		return image;
	}

	//TODO eliminate
	public void render(Board board) {
		if (board == null) throw new IllegalArgumentException("null board");
		render(board.pieces);
	}

	public void render(Pieces pieces) {
//		Line2D.Double line = new Line2D.Double();
//		g.setStroke(STROKE);
		g.setFont(CHESS_FONT);
		g.setColor(Color.BLACK);
		for (int rank = 0; rank < 8; rank++) {
			for (int file = 0; file < 8; file++) {
				Square sqr = Square.at(file, rank);
				String str;
				Piece piece = pieces.get(sqr);
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

	public void render(PositionMoves moves) {
		new MoveRenderer(moves).render();
	}
	
	public void render(Sequence sequence) {
		sequence.forEach( p -> new MoveRenderer(p.previous().moves()).render(p.previousMove(), Integer.toString(p.moveNumber)), 1, sequence.length());
	}
	
	public void render(Sequence sequence, Colour colour) {
		sequence.forEach( p -> {
			Position previous = p.previous();
			if (previous.toMove == colour) {
				new MoveRenderer(previous).render(p.previousMove(), Integer.toString(p.moveNumber));
			}
		}, 1, sequence.length());
	}
	
	private static Color translucent(Color c, float alpha) {
		int a = (int) (alpha * 255) << 24;
		return new Color(c.getRGB() & 0x00ffffff | a, true);
	}
	
	private void renderBackground() {
		g.setTransform(boardTrans);
		g.setColor(Color.WHITE);
		g.fill(SQUARE);
	}

	private void renderCoords() {
		g.setTransform(boardTrans);
		g.setColor(Color.BLACK);
		double inset = BOARD_INSET * 0.7;
		for (File file : File.values()) {
			renderCoord(centerCoord(file), -inset, file.character);
			renderCoord(centerCoord(file), 1 + inset, file.character);
		}
		for (Rank rank : Rank.values()) {
			renderCoord(   -inset, centerCoord(rank), rank.character);
			renderCoord(1 + inset, centerCoord(rank), rank.character);
		}
	}
	
	private void renderCoord(double x, double y, char character) {
		String str = Character.toString(character).toUpperCase();
		float width = (float) g.getFontMetrics().getStringBounds(str, g).getWidth();
		//float ascent = CHESS_FONT.getLineMetrics("str", g.getFontRenderContext()).getAscent();
		float ascent = 0.002f;
		float sx = (float) (x - width / 2.0);
		float sy = (float) (y + 8 * ascent);
		g.drawString(str, sx, sy);
	}

	private static Point2D.Double center(Square square) {
		return new Point2D.Double(centerCoord(square.file), centerCoord(square.rank));
	}
	
	private static double centerCoord(File file) {
		return (file.ordinal() + 0.5) / 8.0;
	}
	
	private static double centerCoord(Rank rank) {
		return (7.0 - rank.ordinal() + 0.5) / 8.0;
	}
	
	private void reset() {
		g.setTransform(boardTrans);
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
		double s = a.distance(b);
		double x = (b.x - a.x) / s;
		double y = (b.y - a.y) / s;
		AffineTransform tr = new AffineTransform();
		tr.translate(a.x, a.y);
		tr.scale(s / length, s / length);
		tr.rotate(x, y);
		return tr;
	}

	private class MoveRenderer {

		private final PositionMoves moves;
		private final Colour colour;
		private final SquareMap<List<Move>> map;
		private final Squares occupied;

		//TODO decide on best constructor
		MoveRenderer(Position position) {
			this(position.moves());
		}

		MoveRenderer(PositionMoves moves) {
			if (moves == null) throw new IllegalArgumentException("null moves");
			this.moves = moves;
			colour = moves.position.toMove;
			map = moves.movesByOriginSquare();
			occupied = moves.position.pieces().keySet();
			g.setStroke(ARROW_STROKE);
			g.setFont(ANN_FONT);
		}
		
		void render() {
			for (Entry<Square,List<Move>> entry : map.entrySet()) {
				render( entry.getValue() );
			}
		}

		void render(List<Move> list) {
			MutableSquares visited = new MutableSquares();
			for (ListIterator<Move> i = list.listIterator(list.size()); i.hasPrevious();) {
				Move move = i.previous();
				render(move, null, visited);
			}
		}

		private void render(Move move, String ann) {
			if (move != null) render(move, ann, new MutableSquares());
		}

		private void render(Move move, String ann, MutableSquares visited) {
			if (visited.contains(move.to)) return;
			Arrow arrow = new Arrow(move, null);
			Color pieceColor = colour.white ? Color.WHITE : Color.BLACK;
			Color contrastColor = colour.white ? Color.BLACK : Color.WHITE;
			Paint fill;
			Paint stroke;
			//TODO not good enough - could be enpasant
			if (occupied.contains(move.to)) {
				fill = new GradientPaint(arrow.base, pieceColor, arrow.point, Color.RED);
				stroke = contrastColor;
			} else {
				fill = pieceColor;
				stroke = contrastColor;
			}
			Shape shape = ann == null ? arrow.shape : arrow.shapeWithAnn;
			if (fill != null) {
				g.setPaint(fill);
				g.fill(shape);
			}
			if (stroke != null) {
				g.setPaint(stroke);
				g.draw(shape);
			}
			if (ann != null) {
				g.setColor(contrastColor);
				g.drawString(ann, (float) (arrow.ann.x - 0.015), (float) (arrow.ann.y + 0.018));
			}
			visited.addAll(move.intermediateSquares);
		}

	}
	
	private static Ellipse2D.Double circle(double cx, double cy, double r) {
		return new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
	}
	
	private static class Arrow {

		final Point2D.Double base;
		final Point2D.Double point;
		final Shape shape;
		final Point2D.Double ann;
		final Shape shapeWithAnn;

		Arrow(Move move, Squares previous) {
			base = center(move.from);
			point = center(move.to);

			double length = base.distance(point) * 8.0;
			Path2D.Double path = new Path2D.Double();
//			if (move.isPossibleFor(PieceType.KNIGHT)) {
//				path.moveTo ( 0.00,  0.50 );
//				path.lineTo ( 0.05,  0.50 ); path.curveTo( 0.05,  1.00, x2, y2, 2.40, 1.00);
//				path.lineTo ( 2.40,  1.00 );
//				path.lineTo ( 2.35,  1.00 );
//				path.lineTo ( 2.50,  1.50 );
//				path.lineTo ( 2.65,  1.00 );
//				path.curveTo( 1.00 * length - 0.50, -0.05 - 0.02 * length );
//				path.lineTo ( 0.50                , -0.05                 );
//				path.closePath();
//			} else {
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
//			}

			AffineTransform trans = transFrom(length, base, point);
			shape = trans.createTransformedShape(path);
			ann = new Point2D.Double();
			trans.transform(new Point2D.Double(except + 0.50, 0), ann);
			Shape circle = circle(ann.x, ann.y, 0.03);
			Area area = new Area(shape);
			area.add(new Area(circle));
			shapeWithAnn = area;
		}
		
	}
	
}
