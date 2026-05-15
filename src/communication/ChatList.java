package communication;

public enum ChatList implements CommandType{
	ERR("ERR"),
	CHAT("CHAT"),
	SERVER("SERVER"),
	;
	private final String label;
	
	private ChatList(String label) {
		this.label=label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
}
