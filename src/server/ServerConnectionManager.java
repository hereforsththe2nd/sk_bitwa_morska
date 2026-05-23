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

interface OutSendListener{
	void onMessage(String target, Command command);
}

public class ServerConnectionManager {
	private ServerConnectionManager server = this;
	private ServerSocket serverSocket; 
	private Thread newConnections;
	private LinkedList<User> users = new LinkedList<User>();
	private boolean running = true;
	private ConnectionListener connectionListener;
	
	private final LinkedList<ClientToServerMessageListener> mesListeners = new LinkedList<ClientToServerMessageListener>();
	private final LinkedList<OutSendListener> outListeners = new LinkedList<OutSendListener>();
	
	public ServerConnectionManager(int port) throws IOException {
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
		addUser(user);
		user.userName = nextDefaultUsername(users);
		if(connectionListener!=null) connectionListener.onConnection(socket, users.getLast());
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
						for(ClientToServerMessageListener messageListener : mesListeners)
							try{
								messageListener.onMessage(com, user);
							}catch (Exception e) {
								e.printStackTrace();
							}
					} catch (IOException e) {
						System.err.println("User disconnected " + user.ID);
						user.connected = false;
						
					}
				}
				try {
					bufferedReader.close();
					removeUser(user);
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
	
	private void sendWT(Command com, User user) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(user.socket.getOutputStream()));
		writer.write(Command.encode(com));
		writer.write("\n");
		writer.flush();
	}
	
	public void send(Command com, User user) throws IOException {
		sendWT(com, user);
		for(OutSendListener listener : outListeners) listener.onMessage(user.userName + ", ID: "+user.ID, com);
	}
	
	protected void sendAll(Command com) throws IOException {
		for(User user : users) sendWT(com, user);
		for(OutSendListener listener : outListeners) listener.onMessage("|ALL|", com);
	}
	
	protected void setConnectionListener(ConnectionListener listener) {
		this.connectionListener=listener;
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
	
	protected void removeMessageListener(ClientToServerMessageListener listener) {
		mesListeners.remove(listener);
	}
	
	protected void addSendListener(OutSendListener listener) {
		outListeners.add(listener);
	}
	
	static private String nextDefaultUsername(LinkedList<User> users) {
		//tworzy defaultową nazwę użytkownika postaci default0, default1, default2, default3, itd. aby liczba na końcu była najmniejsza możliwa
		//nieidealny, ale w żadnej sytuacji okropny
		final String defaultName = "default";
		String ret;
		int i=-1;
		for(User user : users) {
			if(user.userName == null) continue;
			if(user.userName.matches(defaultName+"[0-9]+")) {
				int ind = Integer.parseInt(String.valueOf(user.userName.substring(defaultName.length(), user.userName.length())));
				if(ind > i) i = ind;
			}
		}
		i++;
		ret = defaultName + i;
		return ret;
	}
	
	public LinkedList<User> getUsers() {
		return users;
	}
	
	protected User getUser(String userName) {
		for(User u : users)
			if(u.userName.equals(userName))
				return u;
		return null;
	}
	
	private void removeUser(User user) {
		synchronized(Server.lock) {
			users.remove(user);
		}
	}
	
	private void addUser(User user) {
		synchronized (Server.lock) {
			users.add(user);
		}
	}
}
