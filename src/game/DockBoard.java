package game;

public class DockBoard extends Board {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6831697083852871746L;

	public DockBoard(int width, int height) {
		super(width, height);
	}

    public void layoutShipsInDock() {
        
        for (int i = 0; i < ships.size(); i++) {
            Ship s = ships.get(i);
            s.pos = new Position(0, i);
            s.horizontal = true;
        }
        refreshGridShips();
    }

    public void startShipPlacement() {
        ships.clear();
        grid.clearLayer(SHIP);
        int[] config = {5,4,4,3,3,2,2};

        for (int len : config) {
            Ship s = new Ship(len);
            addShip(s);
        }
        
        layoutShipsInDock();
    }

}