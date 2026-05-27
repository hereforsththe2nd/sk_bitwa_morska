package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import game.Ship.BooleanPointer;

public final class Drawables {	
	static final class Tiles implements Drawable{
		private static final Color color1=new Color(192,149,55);
		private static final Color color2=new Color(150,124,68);
		private final Position r;
		@Override
		public void draw(Rectangle bounds, Graphics2D g2d) {
			if((r.x+r.y)%2 == 0) g2d.setColor(color1);
			else g2d.setColor(color2);
			//g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			//double x1=bounds.x, y1=bounds.y+0.5, x2=(bounds.x+bounds.width),y2=(bounds.y+bounds.height)+0.5;
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
		public Position getPosition() {
			return r;
		}
		
		public Tiles(Position r) {
			this.r = r;
		}

	}
	static final class Hover implements Drawable{
		private final Color hoverColor = new Color(51, 49, 43, 150);
		private Position r;
		@Override
		public void draw(Rectangle bounds, Graphics2D g2d) {
			if(r == null) return;
			g2d.setColor(hoverColor);
			g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		@Override
		public Position getPosition() {
			return r;
		}

		public void setPosition(Position r) {
			this.r = r;
		}
	}
	public static final class X implements Drawable{
		private final Position r;
		private final int pad = 4;
		public X(Position r) {
			this.r=r;
		}
		
		@Override
		public void draw(Rectangle bounds, Graphics2D g2d) {
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(2));
			g2d.drawLine(bounds.x+pad, bounds.y+pad, bounds.x+bounds.width-pad, bounds.y+bounds.height-pad);
			g2d.drawLine(bounds.x+bounds.width - pad, bounds.y+pad, bounds.x+pad, bounds.y+bounds.height-pad);
		}

		@Override
		public Position getPosition() {
			return r;
		}
		
	}
	public static final class Flash implements Drawable{
		private final Position r;
		static public final Color COLOR = new Color(0,200,0,128);
		private Color color;
		
		
		public Flash(Position r) {
			this.r=r;
			this.color=COLOR;
		}
		
		public Flash(Position r, Color color) {
			this.r=r;
			this.color=color;
		}
		
		@Override
		public void draw(Rectangle bounds, Graphics2D g2d) {
			g2d.setColor(COLOR);
			g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		@Override
		public Position getPosition() {
			return r;
		}
		
	}
	public static final class ShipTile implements Drawable{
		private final BooleanPointer valid;
		private final BooleanPointer horizontal;
		private final Position dp;
		private final Position shipP;
		
		public ShipTile(Position dp, Position shipP,  BooleanPointer valid, BooleanPointer horizontal) {
			this.valid=valid;
			this.dp=dp;
			this.shipP=shipP;
			this.horizontal=horizontal;
		}
		
		@Override
		public void draw(Rectangle bounds, Graphics2D g2d) {
            if(valid.value) {
                g2d.setColor(Color.blue);
            } else {
                g2d.setColor(Color.red);
            }
         
            g2d.fillRoundRect(
                bounds.x + 2,
                bounds.y + 2,
                bounds.width - 4,
                bounds.height - 4,
                10,
                10
            );

         
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(
                bounds.x + 2,
                bounds.y + 2,
                bounds.width - 4,
                bounds.height - 4,
                10,
                10
            );
		}


		@Override
		public Position getPosition() {
			if(horizontal == null || horizontal.value)
				return new Position(dp.x+shipP.x, dp.y+shipP.y);
			return new Position(shipP.x-dp.y, shipP.y+dp.x);
		}
		
	}
	
	public static final class Text implements Drawable{
		private final String text;
		private final Position p;
		
		public Text(String str, Position p) {
			this.text=str;
			this.p=p;
		}
		
		@Override
		public void draw(Rectangle bounds, Graphics2D g2d) {
			FontMetrics fm = g2d.getFontMetrics();
			Rectangle rect = fm.getStringBounds(text, 0, text.length(), g2d).getBounds();
			g2d.drawString(text, bounds.x+(bounds.width-rect.width)/2, bounds.y + (bounds.height+rect.height)/2);
		}

		@Override
		public Position getPosition() {
			return p;
		}
		
	}
}
