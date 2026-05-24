package game;

import java.awt.Color;
import java.awt.Graphics2D;
import game.Drawable;

public class Ship implements Drawable {

    public int length;
    public Position pos;
    public boolean horizontal = true;

    public Ship(int length) {
        this.length = length;
    }

    public void rotate() {
        horizontal = !horizontal;
    }

    public boolean occupies(Position p) {
        if (pos == null) return false;

        for (int i = 0; i < length; i++) {
            int x = pos.x + (horizontal ? i : 0);
            int y = pos.y + (horizontal ? 0 : i);

            if (x == p.x && y == p.y) return true;
        }
        return false;
    }

    @Override
    public void draw(Rectangle bounds, Graphics2D g2d) {
        if (pos == null) return;

        g2d.setColor(new Color(0, 120, 255, 160));

        for (int i = 0; i < length; i++) {
        	int cellW = bounds.width;
        	int cellH = bounds.height;

        	int startX = bounds.x;
        	int startY = bounds.y;

        	g2d.fillRect(
        	    startX + (horizontal ? i * cellW : 0),
        	    startY + (horizontal ? 0 : i * cellH),
        	    cellW,
        	    cellH
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
}