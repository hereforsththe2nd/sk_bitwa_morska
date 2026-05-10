package server;

abstract public class Command<E extends Enum<E> & Command.Labeled> {
	
	interface Labeled{
		String getLabel();
	}
	
    private final Class<E> enumClass;
    protected E List;
	protected final E command;
	protected final String message;
	private static final char separator = '|';
	protected Command(String str, Class<E> enumClass) {
		//dekoduje str
		
		this.enumClass=enumClass;
		
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
		for(E com : enumClass.getEnumConstants()) {
			if(com.equals(command)) {
				this.command = com;
				this.message = message;
				return;
			}
		}
		this.command= commandError();
		this.message=str;
	}
	Command(E command, String message, Class<E> enumClass){
		this.enumClass = enumClass;
		this.command=command;
		this.message=message;
	}
	public String encode() {
		return this.command.getLabel() + separator + this.message;
	}
	abstract protected E commandError(); 
}
