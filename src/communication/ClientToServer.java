package communication;

public enum ClientToServer implements CommandType{
	SET_USERNAME("SET_USERNAME"),
	CHAT("CHAT"),
	;
	private final String label;
	private ClientToServer(String label) {
		this.label = label; 
	}
	@Override
	public String getLabel() {
		return label;
	}
	
}
