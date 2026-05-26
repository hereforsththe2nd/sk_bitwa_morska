package game;

import java.util.LinkedList;
import java.util.List;

public class PlayerBoard extends Board {
	public static final int N =  10;
	/**
	 * 
	 */
	private static final long serialVersionUID = -5130784898449876884L;

	public PlayerBoard(int width, int height) {
		super(width, height, N);
	}

	public boolean isValid(Ship dragged) {
		return dragged.thisIsValid(ships, N);
	}

	public void clearShips() {
		for(Ship s : (List<Ship>)ships.clone()) {
			removeShip(s);
		}
		ships.clear();
	}

	public String encodeShipLocations() {
		String str = "";
    	for(Ship ship : ships) {
    		str += ship+"|";
    	}
    	return str;
	}

}
