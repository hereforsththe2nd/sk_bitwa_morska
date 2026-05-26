package game;

import java.util.Comparator;

public class DockBoard extends Board {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6831697083852871746L;
	public static final Integer[] CONFIG = {3,3,2};//{4,3,3,2,2,2,1,1,1,1};
	
	public DockBoard(int width, int height) {
		super(width, height, CONFIG.length);
	}

    public void layoutShipsInDock() {
        ships.sort(Comparator.comparing(Ship::getLength) );
    	ships = ships.reversed();
        for (int i = 0; i < ships.size(); i++) {
            Ship s = ships.get(i);
            s.pos = new Position(i, i);
        }
        refreshGridShips();
    }

    public void startShipPlacement() {
        ships.clear();
        grid.clearLayer(SHIP);

        for (int len : CONFIG) {
            Ship s = new Ship(len);
            addShip(s);
        }
        
        layoutShipsInDock();
    }

}