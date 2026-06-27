package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class DockBoard extends Board {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6831697083852871746L;
	public static final Integer[] CONFIG = {4,3,3,2,2,2,1,1,1,1};//ułożone malejąco koniecznie!

	private static int minSize() {
		int min = 0;
		for(int i=0;i<CONFIG.length;i++) {
			if(i+CONFIG[i] > min)
				min = i+CONFIG[i];
		}
		return min;
	}
	public DockBoard(int width, int height) {
		super(width, height, minSize());
	}

    public void layoutShipsInDock() {
        ships.sort(Comparator.comparing(Ship::getLength) );
    	Collections.reverse(ships);
        for (int i = 0; i < ships.size(); i++) {
            Ship s = ships.get(i);
            s.pos.setValues(i, i);
        }
        refreshGridShips();
    }

    public void startShipPlacement() {
        ships.clear();
        getGrid().clearLayer(SHIP);

        for (int len : CONFIG) {
            Ship s = new Ship(len);
            addShip(s);
        }
        
        layoutShipsInDock();
    }


}