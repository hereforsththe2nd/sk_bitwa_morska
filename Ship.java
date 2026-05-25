package game;

import java.awt.Color;
import java.awt.Graphics2D;
import game.Drawable;

public class Ship implements Drawable {

    public int length;
    public Position pos;
    public boolean horizontal = true;

    private boolean valid = true;
    
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

	public void setValid(boolean b) {
		valid = b;
	}
}