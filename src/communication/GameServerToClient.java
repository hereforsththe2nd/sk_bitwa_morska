package communication;

public enum GameServerToClient implements CommandType{
	YOU_WON("WON"),
	YOU_LOST("LOST"),
	PHASE_SHOOT("SHOOT"),
	PHASE_AWAIT("AWAIT"),
	WRONG_SHIP_CONFIGURATION("WSC"),
	STRIKE_HIT("HIT"),
	STRIKE_MISS("MISSED"),
	ENEMY_STRIKE("ENEMY_STIKE"),
	ERROR("ERROR"),
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
