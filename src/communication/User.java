package communication;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Random;

public class User {
	static LinkedList<Integer> IDs = new LinkedList<Integer>();
	Random rand = new Random();
	public final int ID;
	public String userName;
	public final Socket socket;
	public boolean connected;
	public User(Socket socket) {
		this.socket = socket;
		int random = rand.nextInt();
		while((IDs.contains(random))) {
			random = rand.nextInt();
		}
		ID = random;
		connected = true;
	}
	@Override
	public boolean equals(Object obj) {
		return obj==this;
	}
	public String print() {
		return "user ID: "+ID + " username: "+userName;
	}
}
