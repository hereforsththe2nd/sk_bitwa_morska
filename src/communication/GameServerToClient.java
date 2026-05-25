package communication;

public enum GameServerToClient implements CommandType{
	YOU_WON("WON"),
	YOU_LOST("LOST"),
	PHASE_SHOOT("SHOOT"),
	PHASE_AWAIT("AWAIT"),
	WRONG_SHIP_CONFIGURATION("WSC"),
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
