package communication;

public class Command {
	static final CommandType PARSING_ERR = new CommandType() {
		@Override
		public String getLabel() {
			return "ERROR_PARSING_MESSAGE";
		}
	};
	public final String message;
	public final CommandType type;
	private static final char separator = '|';
	protected Command(String str, CommandType[] list) {
		//dekoduje str

		String command;
		String message;
		int i = str.indexOf(separator);
		switch (i) {
		case -1:
			command = str;
			message=null;
			break;

		default:
			command = str.substring(0, i);
			message = str.substring(i+1);
			break;
		}
		for(CommandType com : list) {
			if(command.equals(com.getLabel())) {
				this.type = com;
				this.message = message;
				return;
			}
		}
		this.type= PARSING_ERR;
		this.message=str;
	}
	Command(CommandType type, String message){
		this.type=type;
		this.message=message;
	}
	
	public static Command decode(String str, CommandType[] list) {
		return new Command(str, list);
	}
	
	public static String encode(CommandType type, String message) {
		return encode(new Command(type, message));
	}
	
	public static String encode(Command com) {
		return com.type.getLabel()+"|"+com.message;
	}
	
}
