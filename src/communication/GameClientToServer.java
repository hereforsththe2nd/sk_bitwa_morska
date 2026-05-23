package communication;

public enum GameClientToServer implements CommandType{
	TEMP_PLEASELETMEWIN("ff"),
	SET_SHIP("SET_SHIP"),
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
