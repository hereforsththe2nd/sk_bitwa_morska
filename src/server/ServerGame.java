package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import communication.Command;
import communication.CommandType;
import communication.GameClientToServer;
import communication.GameServerToClient;
import communication.ServerToClient;
import communication.User;
import game.BasicShip;
import game.DockBoard;
import game.Phase;
import game.PlayerBoard;

public class ServerGame {
	public static final LinkedList<ServerGame> games = new LinkedList<ServerGame>();
	public final ArrayList<User> users;
	private final Map<User, LinkedList<BasicShip>> ships = new HashMap<>();
	private final Map<User, Phase> phases = new HashMap<>();
	private final ServerConnectionManager conn;
	public ServerGame(User user1, User user2, ServerConnectionManager conn) {
		users =  new ArrayList<User>(List.of(new User[] {user1, user2})); 
		ships.put(user1, new LinkedList<BasicShip>());
		ships.put(user2, new LinkedList<BasicShip>());
		phases.put(user1, Phase.SETTING_SHIPS);
		phases.put(user2, Phase.SETTING_SHIPS);
		this.conn = conn;
	}
	
	public boolean isPlaying(User user) {
		return users.contains(user);
	}

	public void send(Command com, User user) throws IOException {
		conn.send(new Command(ServerToClient.GAME, Command.encode(com)), user);
	}
	
	public void exec(Command command, User user) throws IOException {
		switch(CommandType.get(command.context, GameClientToServer.values())) {
		case GameClientToServer.TEMP_PLEASELETMEWIN:
			endGame(user);
			break;
		case GameClientToServer.SET_SHIP:
			LinkedList<Integer> lengths = new LinkedList<Integer>();
			ships.get(user).clear();
			for(String str : command.body.split("\\|")) {
				System.out.println(str);
				System.out.println(command.body);
				ships.get(user).add( BasicShip.fromString(str) ); 
				lengths.add(ships.get(user).getLast().getLength());
			}
			boolean valid = BasicShip.isValid(ships.get(user), PlayerBoard.N);
			lengths.sort(null);
			lengths = lengths.reversed();
			
			if(!lengths.equals( new LinkedList<Integer>( List.of( DockBoard.CONFIG )) )) {
				send(new Command(GameServerToClient.WRONG_SHIP_CONFIGURATION, "Niepoprawna ilość statków"), user);
				return;
			}
			if(!valid) {
				send(new Command(GameServerToClient.WRONG_SHIP_CONFIGURATION, "Niepoprawne rozstawienie statków."), user);
				return;
			}
			if(phases.get(otherUser(user)) == Phase.SETTING_SHIPS)
				send(new Command(GameServerToClient.PHASE_AWAIT, null), user);
			else
				send(new Command(GameServerToClient.PHASE_SHOOT, null), user);
			break;
		default:
			
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

	protected void disconeccted(User user) {
		if(!users.contains(user))
			return;
		games.remove(this);
		try {
			send(new Command(GameServerToClient.YOU_WON, " opponent disconnected"), otherUser(user));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
