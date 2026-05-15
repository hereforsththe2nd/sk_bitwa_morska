package communication;

public class Command {
	public final String body;
	public final String context;
	private static final char separator = '|';
			
	protected Command(String str) {
		//dekoduje str
		int i = str.indexOf(separator);
		switch (i) {
		case -1:
			context = str;
			body=null;
			break;

		default:
			context = str.substring(0, i);
			body = str.substring(i+1);
			break;
		}
	}
	
	public Command(CommandType type, String body){
		this.context=type.getLabel();
		this.body=body;
	}
	
	public Command(String context, String body) {
		this.body = body;
		this.context = context;
	}
	
	public static Command decode(String str) {
		return new Command(str);
	}
		
	public static String encode(String context, String message) {
		return encode(new Command(context, message));
	}
	
	public static String encode(Command com) {
		return com.context+"|"+com.body;
	}
	
	public static String encode(CommandType type, String body) {
		return Command.encode(new Command(type.getLabel(), body));
	}
}
