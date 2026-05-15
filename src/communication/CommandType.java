package communication;

public interface CommandType {
	String getLabel();
	
	public static final CommandType PARSING_ERR = new CommandType() {
		@Override
		public String getLabel() {
			return "ERROR_PARSING_MESSAGE";
		}
	};

	static CommandType get(String context, CommandType[] list) {
		for(CommandType type : list)
			if(type.getLabel().equals(context)) return type;
		return CommandType.PARSING_ERR;
	}
}
