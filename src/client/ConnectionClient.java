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


public class ConnectionClient {
	static public interface ConnectionListener{
		void onMessage(Command com);
	}
	
	
	static public interface AutoStopMessageListener extends ConnectionListener{
		ServerToClient getContext();
	}
	
	final Socket socket;
	final BufferedWriter writer;
	final BufferedReader reader;
	final LinkedList<ConnectionListener> listeners = new LinkedList<ConnectionListener>();
	final LinkedList<AutoStopMessageListener> autoStopListeners = new LinkedList<AutoStopMessageListener>();
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
							synchronized(Locks.ADD_LISTENER) {
								for(ConnectionListener listener : listeners) listener.onMessage(com);
								for(AutoStopMessageListener listener : (LinkedList<AutoStopMessageListener>)autoStopListeners.clone()) {
									listener.onMessage(com);
									if(com.context.equals(listener.getContext().getLabel())) autoStopListeners.remove(listener);
								}
							}
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
	
	public void addMessageListener(ConnectionListener listener) {
		synchronized(Locks.ADD_LISTENER) {
			listeners.add(listener);
		}
	}
	
 	public void send(Command com) throws IOException {
		writer.write(Command.encode(com));
		writer.write("\n");
		writer.flush();
	}
 	
 	public void sendAndAwait(Command com, AutoStopMessageListener listener) throws IOException {
 		//ważna jest kolejność w której te rzeczy się robi!
 		synchronized (Locks.ADD_LISTENER) {
			autoStopListeners.add(listener);
		}
 		send(com);
 	}
}
