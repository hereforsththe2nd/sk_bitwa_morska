package game;

import communication.User;

public class ServerGame {
	public final User[] users;
	
	public ServerGame(User user1, User user2) {
		users = new User[] {user1, user2};
	}
}
