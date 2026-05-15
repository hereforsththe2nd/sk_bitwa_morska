package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import communication.Command;
import communication.ServerToClient;

interface ConnectionListener{
	void onMessage(Command com);
}

public class ConnectionClient {
	final Socket socket;
	final BufferedWriter writer;
	final BufferedReader reader;
	final LinkedList<ConnectionListener> listeners = new LinkedList<ConnectionListener>();
	boolean connected = true;

	protected ConnectionClient(Socket socket) throws UnknownHostException, IOException {
			this.socket = socket;
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			Thread listen = new Thread(new Runnable() {
				String line;
				Command com;
				@Override
				public void run() {
					while(connected) {
						try {
							line=reader.readLine();
							if(line == null) {
								connected=false;
								continue;
							}
							com = Command.decode(line);
							for(ConnectionListener listener : listeners) listener.onMessage(com);
						} catch (IOException e) {
							connected = false;
						}
					}
					try {
						System.out.println("Zmaknieto (klient)");
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			listen.start();
	}

	protected void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connected = false;
	}
	
	protected void addConnectionListener(ConnectionListener listener) {
		listeners.add(listener);
	}
	
 	protected void send(Command com) throws IOException {
		writer.write(Command.encode(com));
		writer.write("\n");
		writer.flush();
	}
}
