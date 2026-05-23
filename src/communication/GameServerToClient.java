package communication;

public enum GameServerToClient implements CommandType{
	YOU_WON("WON"),
	YOU_LOST("LOST"),
	;
	private final String label;
	private GameServerToClient(String label) {
		this.label = label;
	}
	@Override
	public String getLabel() {
		return label;
	}

}
