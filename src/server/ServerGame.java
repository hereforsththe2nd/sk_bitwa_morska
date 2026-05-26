package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import communication.ChatList;
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
import game.Position;

public class ServerGame {
	public static final LinkedList<ServerGame> games = new LinkedList<ServerGame>();
	public final ArrayList<User> users;
	private final Map<User, LinkedList<BasicShip>> ships = new HashMap<>();
	private final Map<BasicShip, Integer> hits = new HashMap<BasicShip, Integer>();
	private final Map<User, Phase> phases = new HashMap<>();
	private final Map<User, LinkedList<Position>> guesses = new HashMap<>();
	private final ServerConnectionManager conn;
	public ServerGame(User user1, User user2, ServerConnectionManager conn) {
		users =  new ArrayList<User>(List.of(new User[] {user1, user2})); 
		ships.put(user1, new LinkedList<BasicShip>());
		ships.put(user2, new LinkedList<BasicShip>());
		phases.put(user1, Phase.SETTING_SHIPS);
		phases.put(user2, Phase.SETTING_SHIPS);
		guesses.put(user1, new LinkedList<Position>());
		guesses.put(user2, new LinkedList<Position>());
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
		case GameClientToServer.FORFEIT:
			endGame(otherUser(user), "Przeciwnik się poddał.", "Poddałeś się.");
			break;
		case GameClientToServer.SET_SHIP:
			if(phases.get(user)!=Phase.SETTING_SHIPS)
				return;
			LinkedList<Integer> lengths = new LinkedList<Integer>();
			for(String str : command.body.split("\\|")) {
				ships.get(user).add( BasicShip.fromString(str) ); 
				lengths.add(ships.get(user).getLast().getLength());
			}
			boolean valid = BasicShip.isValid(ships.get(user), PlayerBoard.N);
			lengths.sort(null);
			lengths = lengths.reversed();
			
			if(!lengths.equals( new LinkedList<Integer>( List.of( DockBoard.CONFIG )) )) {
				ships.get(user).clear();
				send(new Command(GameServerToClient.WRONG_SHIP_CONFIGURATION, "Niepoprawna ilość statków"), user);
				return;
			}
			if(!valid) {
				ships.get(user).clear();
				send(new Command(GameServerToClient.WRONG_SHIP_CONFIGURATION, "Niepoprawne rozstawienie statków."), user);
				return;
			}
			for(BasicShip s : ships.get(user)) {
				hits.put(s, 0);
			}
			if(phases.get(otherUser(user)) == Phase.SETTING_SHIPS) {
				phases.put(user, Phase.AWAITING_MISSLE);
				send(new Command(GameServerToClient.PHASE_AWAIT, null), user);
			}
			else {
				phases.put(user, Phase.SENDING_MISSLE);
				send(new Command(GameServerToClient.PHASE_SHOOT, null), user);
			}
			break;
			
		case GameClientToServer.STRIKE:
			if(phases.get(user)!=Phase.SENDING_MISSLE)
				return;
			Position p = Position.decode(command.body);
			if(!p.checkIfContained(PlayerBoard.N, PlayerBoard.N)) {
				send(new Command(GameServerToClient.ERROR, "Coś poszło nie tak: punkt poza planszą"), user);
				return;
			}
			if(guesses.get(user).contains(p)) {
				send(new Command(GameServerToClient.ERROR, "Już raz zgadnięto ten punkt"), user);
				return;
			}
			guesses.get(user).add(p);
			send(new Command(GameServerToClient.ENEMY_STRIKE, Position.encode(p)), otherUser(user));
			for(BasicShip s : ships.get(otherUser(user))) {
				if(s.occupies(p)) {
					send(new Command(GameServerToClient.STRIKE_HIT, Position.encode(p)+""), user);
					hits.put(s, hits.get(s)+1);
					if(hits.get(s).equals(s.getLength())) {
						for(Position around : s.around(PlayerBoard.N, PlayerBoard.N)) {
							if(!guesses.get(user).contains(around)) {
								guesses.get(user).add(around);
								send(new Command(GameServerToClient.STRIKE_MISS, Position.encode(around)), user);
								send(new Command(GameServerToClient.ENEMY_STRIKE, Position.encode(around)), otherUser(user));
							}
						};
						for(BasicShip cs : ships.get(otherUser(user))) {
							if(!hits.get(cs).equals(cs.getLength())) {
								return;
							}
						}
						endGame(user, "Wygrałeś!", "Przegrałeś :(");
						return;
					}
					return;
				}
			}
			send(new Command(GameServerToClient.STRIKE_MISS, Position.encode(p)), user);
			send(new Command(GameServerToClient.PHASE_AWAIT, null), user);
			send(new Command(GameServerToClient.PHASE_SHOOT, null), otherUser(user));
			phases.put(user, Phase.AWAITING_MISSLE);
			phases.put(otherUser(user), Phase.SENDING_MISSLE);
			break;
		default:
			
		}
	}
	
	public void endGame(User winner, String winMessage, String loseMessage) {
		games.remove(this);
		try {
			send(new Command(GameServerToClient.YOU_WON, winMessage), winner);
			send(new Command(GameServerToClient.YOU_LOST, loseMessage), otherUser(winner));
			conn.sendAll(new Command(ServerToClient.CHAT, Command.encode(ChatList.SERVER, "Użytkownik "+winner.userName + " wygrał w pojedynu z użytkownikiem " + otherUser(winner).userName+"!")));
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
