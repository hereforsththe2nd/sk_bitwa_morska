package communication;

public enum ServerToClient implements CommandType{
	CHAT("CHAT"),
	ERROR_PANE("ERROR_PANE"),
	GAME("GAME"), 
	USERLIST("USERLIST"),
	START_GAME("START_GAME"),
	YOUR_USERNAME("YOUR_USERNAME")
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
