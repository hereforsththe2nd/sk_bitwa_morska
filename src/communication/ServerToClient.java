package communication;

public enum ServerToClient implements CommandType{
	CHAT("CHAT"),
	ERROR_PANE("ERROR_PANE"),
	GAME("GAME"), 
	USERLIST("USERLIST"),
	START_GAME("START_GAME"),
	;
	private final String label;
	private ServerToClient(String label) {
		this.label = label;
	}
	@Override
	public String getLabel() {
		return label;
	}
}
