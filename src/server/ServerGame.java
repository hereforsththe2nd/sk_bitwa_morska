package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import communication.Command;
import communication.GameClientToServer;
import communication.GameServerToClient;
import communication.ServerToClient;
import communication.User;

public class ServerGame {
	public static final LinkedList<ServerGame> games = new LinkedList<ServerGame>();
	public final ArrayList<User> users;
	private final ServerConnectionManager conn;
	public ServerGame(User user1, User user2, ServerConnectionManager conn) {
		users =  new ArrayList<User>(List.of(new User[] {user1, user2})); 
		this.conn = conn;
	}
	
	public boolean isPlaying(User user) {
		return users.contains(user);
	}

	public void send(Command com, User user) throws IOException {
		conn.send(new Command(ServerToClient.GAME, Command.encode(com)), user);
	}
	
	public void exec(Command command, User user) {
		if(command.isContext(GameClientToServer.TEMP_PLEASELETMEWIN)) {
			endGame(user);
		}
	}
	
	public void endGame(User winner) {
		games.remove(this);
		try {
			send(new Command(GameServerToClient.YOU_WON, winner.userName), winner);
			send(new Command(GameServerToClient.YOU_LOST, otherUser(winner).userName), otherUser(winner));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private User otherUser(User user) {
		int i = users.indexOf(user);
		if(i == -1)
			throw new IllegalArgumentException("Użytkownik "+user+" nie jest graczem w tej grze.");
		return users.get((i+1)%2);
	}
}
