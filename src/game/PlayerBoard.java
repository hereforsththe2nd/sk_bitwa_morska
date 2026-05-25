package game;

public class PlayerBoard extends Board {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5130784898449876884L;

	public PlayerBoard(int width, int height) {
		super(width, height);
	}

	public boolean isValid(Ship dragged) {
		return dragged.thisIsValid(ships, N);
	}

}
