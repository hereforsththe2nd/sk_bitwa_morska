package server;

import server.Command.Labeled;

public class ClientToServerCommand  extends Command<ClientToServerCommand.List>{

	public enum List implements Labeled{
		SET_USERNAME("SET_USERNAME"),
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
	
	protected ClientToServerCommand(String str) {
		super(str, List.class);
	}
	
	protected ClientToServerCommand(List command, String message) {
		super(command, message, List.class);
	}

	@Override
	protected List commandError() {
		return List.ERROR_PARSING_MESSAGE;
	}

}
