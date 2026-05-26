package communication;

public enum GameClientToServer implements CommandType{
	FORFEIT("ff"),
	SET_SHIP("SET_SHIP"),
	STRIKE("STRIKE"),
	;

	private final String label;
	
	 private GameClientToServer(String label) {
		 this.label = label;
	}	
	@Override
	public String getLabel() {
		return label;
	}
	
}
