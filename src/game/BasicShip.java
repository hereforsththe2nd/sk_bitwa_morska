package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

public class BasicShip {
    public int length;
    public Position pos;
    public boolean horizontal = true;

    boolean valid = true;
    
    public BasicShip(int length) {
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


	public void setValid(boolean b) {
		valid = b;
	}
		
    public boolean thisIsValid(List<Ship> ships, int N) {
        for (int i = 0; i < this.length; i++) {
            int x = this.pos.x + (this.horizontal ? i : 0);
            int y = this.pos.y + (this.horizontal ? 0 : i);

            if (x < 0 || y < 0 || x >= N || y >= N)
                return false;

            for (Ship other : ships) {
                if (other == this || other.pos == null) continue;

                for (int j = 0; j < other.length; j++) {
                    int ox = other.pos.x + (other.horizontal ? j : 0);
                    int oy = other.pos.y + (other.horizontal ? 0 : j);

         
                    if (Math.abs(ox - x) <= 1 && Math.abs(oy - y) <= 1)
                        return false;
                }
            }
        }
        return true;
    }
    
    static public boolean isValid(List<Ship> ships, int N) {
    	//niezoptymalizowane, ale nie ma takiej potrzeby
    	for(Ship s : ships) {
    		if(!s.thisIsValid(ships, N))
    			return false;
    	}
    	return true;
    }

	@Override
	public String toString() {
		return ""+ pos.x+","+pos.y + ","+ length+","+(horizontal ? "H" : "V");
	}

	static public BasicShip fromString(String str) {
		String[] sub = str.split(",");
		if(sub.length != 4)
			throw new IllegalArgumentException("Podany string: "+str);
		int posx = Integer.parseInt(sub[0]);
		int posy = Integer.parseInt(sub[1]);
		int lenght = Integer.parseInt(sub[2]);
		if(! (sub[3].equals("H") || sub[3].equals("V") ))
			throw new IllegalArgumentException("Podany string: "+str);
		boolean horizontal = sub[3].equals("H");
		BasicShip b = new BasicShip(lenght);
		b.horizontal=horizontal;
		b.pos=new Position(posx,posy);
		return b;
	}
}
