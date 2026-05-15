package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import communication.ClientToServer;
import communication.Command;
import communication.User;

interface ConnectionListener{
	//listener do sluchania nowych polaczen z uzytkownikami
	void onConnection(Socket socket, User user);
	void onDisconnect(Socket socket, User user);
}

interface ClientToServerMessageListener{
	void onMessage(Command command, User user);
}

public class ConnectionManager {
	ConnectionManager server = this;
	ServerSocket serverSocket;
	Thread newConnections;
	LinkedList<User> users = new LinkedList<User>();
	boolean running = true;
	ConnectionListener connectionListener;
	
	LinkedList<ClientToServerMessageListener> mesListeners = new LinkedList<ClientToServerMessageListener>();
	public ConnectionManager(int port, ConnectionListener listener) throws IOException {
		this.connectionListener = listener;
		serverSocket = new ServerSocket(port);
		newConnections = new Thread(new Runnable() {
			@Override
			public void run() {
			       	while(running) {
				       	Socket socket;
						try {
							socket = serverSocket.accept();
							server.addSocket(socket);
						} catch (IOException e) {
							e.printStackTrace();
							server.close();
							break;
						}
			       	}				
			}
		});
		newConnections.start();
	}
	private void addSocket(Socket socket) throws IOException {
		User user = new User(socket);
		users.add(user);
		user.userName = nextDefaultUsername(users);
		connectionListener.onConnection(socket, users.getLast());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		//BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		Thread recieveMessages = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(running && user.connected) {
					try {
						System.out.println("Listening for messages from user " + user.print());
						String line = bufferedReader.readLine();
						System.out.println(user.print() + " " + line);
						if(line==null) {
							user.connected=false;
							continue;
						}
						Command com = Command.decode(line);
						for(ClientToServerMessageListener messageListener : mesListeners)	messageListener.onMessage(com, user);
					} catch (IOException e) {
						System.err.println("User disconnected " + user.ID);
						user.connected = false;
						
					}
				}
				try {
					bufferedReader.close();
					users.remove(user);
					System.out.println("Succesfully closed connection to user " + user.userName);
					connectionListener.onDisconnect(socket, user);
				} catch (IOException e) {
					System.err.println("Unable to close bufferedReader of user "+user.userName);
					e.printStackTrace();
				}
			}
		}); 
		recieveMessages.start();
	}
	
	protected void send(Command com, User user) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(user.socket.getOutputStream()));
		writer.write(Command.encode(com));
		writer.write("\n");
		writer.flush();
	}
	
	protected void sendAll(Command com) throws IOException {
		for(User user : users) send(com, user);
	}
	
	protected void close() {
		running=false;
		try {
			serverSocket.close();
			System.out.println("Server succesfully closed.");
		} catch (IOException e) {
			System.err.println("Did not manage to close ServerSocket "+serverSocket);
			e.printStackTrace();
		}
	}
	
	protected void addMessageListener(ClientToServerMessageListener listener) {
		mesListeners.add(listener);
	}
	
	static private String nextDefaultUsername(LinkedList<User> users) {
		//tworzy defaultową nazwę użytkownika postaci default0, default1, default2, default3, itd. aby liczba na końcu była najmniejsza możliwa
		//nieidealny, ale w żadnej sytuacji okropny
		final String defaultName = "default";
		String ret;
		int i = -1;
		for(User user : users) {
			if(user.userName == null) continue;
			if(user.userName.length() == defaultName.length()+1) {
				if(user.userName.matches(defaultName+"[0-9]*")) {
					int ind = Integer.parseInt(String.valueOf(user.userName.charAt(user.userName.length()-1)));
					if(ind > i) i = ind;
				}
			}
		}
		i++;
		ret = defaultName + i;
		return ret;
	}
}
