package server;

import java.util.LinkedList;
import java.util.Random;

public class User {
	static LinkedList<Integer> IDs = new LinkedList<Integer>();
	Random rand = new Random();
	final int ID;
	String userName;
	boolean connected;
	public User() {
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
