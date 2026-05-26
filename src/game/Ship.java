package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;

import game.Drawable;
import game.Drawables.ShipTile;

public class Ship extends BasicShip {
	
	final LinkedList<Drawable> tiles = new LinkedList<Drawable>();
	
    public Ship(int length) {
		super(length);
		for(int i=0;i<length;i++) {
			tiles.add(new ShipTile(new Position(i,0), pos, valid, horizontal));
		}
	}
    /*
	@Override
    public void draw(Rectangle bounds, Graphics2D g2d) {
        if (pos == null) return;

        int cellW = bounds.width;
        int cellH = bounds.height;

        for (int i = 0; i < length; i++) {

            int x = bounds.x + (horizontal ? i * cellW : 0);
            int y = bounds.y + (horizontal ? 0 : i * cellH);

        
            if(valid) {
                g2d.setColor(Color.blue);
            } else {
                g2d.setColor(Color.red);
            }
         
            g2d.fillRoundRect(
                x + 2,
                y + 2,
                cellW - 4,
                cellH - 4,
                10,
                10
            );

         
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(
                x + 2,
                y + 2,
                cellW - 4,
                cellH - 4,
                10,
                10
            );
        }
    }
	
    @Override
    public int layer() {
        return Board.SHIP;
    }

    @Override
    public Position getPosition() {
        return pos;
    }
    */
    public static class BooleanPointer{
    	boolean value;
    	public BooleanPointer(boolean value) {
    		this.value=value;
    	}
    }
}