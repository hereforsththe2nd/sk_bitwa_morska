package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

final class Drawables {	
	final static int HORIZONTAL = 0,
			VERTICAL = 1;
	@Deprecated
	private final static void drawLine(int x, int y, int l, int dir, BufferedImage img, Color color) {
		switch(dir) {
		case HORIZONTAL:
			for(int dx=0;dx<l;dx++) {
				try {
					img.setRGB(x+dx, y, color.getRGB());
				}catch (Exception e) {
					System.out.println("Exception occured at " + (x+dx) + ", "+y);
					throw e;
				}
			}
			break;
		case VERTICAL:
			for(int dy=0;dy<l;dy++) {
				img.setRGB(x, y+dy, color.getRGB());
			}
			break;
		default:
			throw new IllegalArgumentException("dir musi być równe "+HORIZONTAL + ", lub "+VERTICAL+", a jest równe "+dir+".");
		}
	}
	static final class Tiles implements Drawable{
		private static final Color color1=new Color(192,149,55);
		private static final Color color2=new Color(150,124,68);
		@Override
		public void draw(Rectangle bounds, Position r, Graphics2D g2d) {
			if((r.x+r.y)%2 == 0) g2d.setColor(color1);
			else g2d.setColor(color2);
			//g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			//double x1=bounds.x, y1=bounds.y+0.5, x2=(bounds.x+bounds.width),y2=(bounds.y+bounds.height)+0.5;
			System.out.println("Tile " + r + " drawn");
			System.out.println("Bounds = "+bounds);
			g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			g2d.setColor(Color.BLACK);
			g2d.drawLine(bounds.x+bounds.width, bounds.y, bounds.x+bounds.width, bounds.y + bounds.height);
			g2d.drawLine(bounds.x, bounds.y+bounds.height, bounds.x+bounds.width, bounds.y+bounds.height);
			
			if(r.x==0)
				g2d.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);
			if(r.y==0)
				g2d.drawLine(bounds.x, bounds.y, bounds.x+bounds.width, bounds.y);
			}

		@Override
		public int layer() {
			return Drawable.BOTTOM_LAYER;
		}

	}
	static final class Hover implements Drawable{
		private final Color hoverColor = new Color(51, 49, 43, 150);
		@Override
		public void draw(Rectangle bounds, Position r, Graphics2D g2d) {
			System.out.println("Drawing hover "+bounds+", "+r);
			g2d.setColor(hoverColor);
			g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		@Override
		public int layer() {
			return Drawable.TOP_LAYER;
		}

	}
}
