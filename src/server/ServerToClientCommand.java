package server;

import server.Command.Labeled;

public class ServerToClientCommand  extends Command<ServerToClientCommand.List>{

	public enum List implements Labeled{
		ERROR_PARSING_MESSAGE("ERROR_PARSING_MESSAGE");
		
		private String label;
		private List(String label) {
			this.label = label;
		}
		@Override
		public String getLabel() {
			return label;
		}
	}
	
	protected ServerToClientCommand(String str) {
		super(str, List.class);
	}
	
	protected ServerToClientCommand(List command, String message) {
		super(command, message, List.class);
	}

	@Override
	protected List commandError() {
		return List.ERROR_PARSING_MESSAGE;
	}

}
