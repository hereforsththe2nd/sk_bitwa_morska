package communication;

public enum ServerToClient implements CommandType{
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
