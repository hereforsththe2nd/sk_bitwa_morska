package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import game.Ship.BooleanPointer;

public class BasicShip {
    public final int length;
    public final Position pos = new Position(0,0);
    BooleanPointer horizontal = new BooleanPointer(true);

    BooleanPointer valid = new BooleanPointer(true);
    
    public BasicShip(int length) {
        this.length = length;
    }

    public void rotate() {
        horizontal.value = !horizontal.value;
    }

    public boolean occupies(Position p) {
        if (pos == null) return false;

        for (int i = 0; i < length; i++) {
            int x = pos.x + (horizontal.value ? i : 0);
            int y = pos.y + (horizontal.value ? 0 : i);

            if (x == p.x && y == p.y) return true;
        }
        return false;
    }


	public void setValid(boolean b) {
		valid.value = b;
	}
		
    public boolean thisIsValid(List<? extends BasicShip> ships, int N) {
        for (int i = 0; i < this.length; i++) {
            int x = this.pos.x + (this.horizontal.value ? i : 0);
            int y = this.pos.y + (this.horizontal.value ? 0 : i);

            if (x < 0 || y < 0 || x >= N || y >= N)
                return false;

            for (BasicShip other : ships) {
                if (other == this || other.pos == null) continue;

                for (int j = 0; j < other.length; j++) {
                    int ox = other.pos.x + (other.horizontal.value ? j : 0);
                    int oy = other.pos.y + (other.horizontal.value ? 0 : j);

         
                    if (Math.abs(ox - x) <= 1 && Math.abs(oy - y) <= 1)
                        return false;
                }
            }
        }
        return true;
    }
    
    static public boolean isValid(List<? extends BasicShip> ships, int N) {
    	//niezoptymalizowane, ale nie ma takiej potrzeby
    	for(BasicShip s : ships) {
    		if(!s.thisIsValid(ships, N))
    			return false;
    	}
    	return true;
    }

	@Override
	public String toString() {
		return ""+ pos.x+","+pos.y + ","+ length+","+(horizontal.value ? "H" : "V");
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
		b.horizontal.value=horizontal;
		b.pos.setValues(posx,posy);
		return b;
	}
	
	public int getLength() {
		return length;
	}
	
	public LinkedList<Position> around(int N1, int N2){
		LinkedList<Position> ret = new LinkedList<Position>();
        for (int i = 0; i < length; i++) {
            int x = pos.x + (horizontal.value ? i : 0);
            int y = pos.y + (horizontal.value ? 0 : i);
            ret.add(horizontal.value ? new Position(x, y+1) : new Position(x+1, y));
            ret.add(horizontal.value ? new Position(x, y-1) : new Position(x-1, y));
            if(i == 0){
            	int sign = 1;
            	ret.add(horizontal.value ? new Position(x-sign, y-1) : new Position(x-1, y-sign));
            	ret.add(horizontal.value ? new Position(x-sign, y) : new Position(x, y-sign));
            	ret.add(horizontal.value ? new Position(x-sign, y+1) : new Position(x+1, y-sign));
            }
            if(i == length-1){
            	int sign = -1;
            	ret.add(horizontal.value ? new Position(x-sign, y-1) : new Position(x-1, y-sign));
            	ret.add(horizontal.value ? new Position(x-sign, y) : new Position(x, y-sign));
            	ret.add(horizontal.value ? new Position(x-sign, y+1) : new Position(x+1, y-sign));
            }

        }
        for(Position p : (LinkedList<Position>)ret.clone()) {
        	if(!p.checkIfContained(N1, N2)){
        		ret.remove(p);
        	}
        }
        return ret;

	}
}
