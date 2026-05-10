package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

interface ConnectionListener{
	//listener do sluchania nowych polaczen z uzytkownikami
	void onConnection(Socket socket, User user);
}

interface ClientToServerMessageListener{
	void onMessage(ClientToServerCommand command, User user);
}

public class Server {
	Server server = this;
	ServerSocket serverSocket;
	Thread newConnections;
	LinkedList<User> users = new LinkedList<User>();
	boolean running = true;
	ConnectionListener listener;
	ClientToServerMessageListener messageListener;
	public Server(int port, ConnectionListener listener, ClientToServerMessageListener messageListener) throws IOException {
		this.listener = listener;
		this.messageListener = messageListener;
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
		User user = new User();
		users.add(user);
		user.userName = nextDefaultUsername(users);
		listener.onConnection(socket, users.getLast());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		//BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		Thread recieveMessages = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(running && user.connected) {
					try {
						String line = bufferedReader.readLine();
						ClientToServerCommand com = new ClientToServerCommand(line);
						messageListener.onMessage(com, user);
					} catch (IOException e) {
						System.err.println("User disconnected " + user.userName);
						user.connected = false;
						
					}
				}
				try {
					bufferedReader.close();
				} catch (IOException e) {
					System.err.println("Unable to close bufferedReader of user "+user.userName);
					e.printStackTrace();
				}
			}
		}); 
		recieveMessages.start();
	}
	protected void close() {
		running=false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("Did not manage to close ServerSocket "+serverSocket);
			e.printStackTrace();
		}
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
	
	public static void main(String[] args) {
		LinkedList<User> users = new LinkedList<User>();
		for(int i=0;i<10;i++) {
			users.add(new User());
			users.getLast().userName = switch(i) {
				case 0 -> {
					yield "default";
				}
				case 1 -> {
					yield "default1";
				}
				case 2 -> {
					yield "default3";
				}
				default -> {
					yield "bruh";
				}
			};
		}
		for(User user : users) {
			System.out.println(user.userName + " <--");
		}
		for(int i=0;i<5;i++) {
			users.add(new User());
			users.getLast().userName = Server.nextDefaultUsername(users);
		}
		for(User user : users) {
			System.out.println(user.userName);
		}
	}
}
