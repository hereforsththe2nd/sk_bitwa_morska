package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import communication.Command;
import communication.ServerToClient;


public class ClientConnectionManager {
	static public interface ConnectionListener{
		void onMessage(Command com);
	}
	
	
	static public interface AutoStopMessageListener extends ConnectionListener{
		boolean stop(Command message);
	}
	final Socket socket;
	final BufferedWriter writer;
	final BufferedReader reader;
	final private LinkedList<ConnectionListener> listeners = new LinkedList<ConnectionListener>();
	final LinkedList<AutoStopMessageListener> autoStopListeners = new LinkedList<AutoStopMessageListener>();
	boolean connected = true;

	protected ClientConnectionManager(Socket socket) throws UnknownHostException, IOException {
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
								for(ConnectionListener listener : (LinkedList<ConnectionListener>)listeners.clone()) listener.onMessage(com);
								for(AutoStopMessageListener listener : (LinkedList<AutoStopMessageListener>)autoStopListeners.clone()) {
									listener.onMessage(com);
									if(listener.stop(com)) autoStopListeners.remove(listener);
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
	
	public void removeMessageListener(ConnectionListener listener) {
		synchronized(Locks.ADD_LISTENER) {
			listeners.remove(listener);
		}
	}
	
	public void addAutoStopMessageListener(AutoStopMessageListener listener) {
 		synchronized (Locks.ADD_LISTENER) {
			autoStopListeners.add(listener);
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
 	
 	private ClientConnectionManager() {
		this.socket = null;
		this.writer = null;
		this.reader = null;
 	}
	static final ClientConnectionManager DISCONNECTED = new ClientConnectionManager() {
		@Override
		public void send(Command com) throws IOException {
			JOptionPane.showMessageDialog(null, "Nie można wykonać czynności: brak połączenia z serwerem.", "Błąd(send)", JOptionPane.ERROR_MESSAGE);
		}
		@Override
		public void sendAndAwait(Command com, AutoStopMessageListener listener) throws IOException {
			JOptionPane.showMessageDialog(null, "Nie można wykonać czynności: brak połączenia z serwerem.", "Błąd (sendAndAwait)", JOptionPane.ERROR_MESSAGE);
		}
		@Override
		public void addAutoStopMessageListener(AutoStopMessageListener listener) {
			JOptionPane.showMessageDialog(null, "Nie można wykonać czynności: brak połączenia z serwerem.", "Błąd, (addAutoStopMessageListener)", JOptionPane.ERROR_MESSAGE);
		}
		@Override
		public void addMessageListener(ConnectionListener listener) {
			JOptionPane.showMessageDialog(null, "Nie można wykonać czynności: brak połączenia z serwerem.", "Błąd, (addMessageListener)", JOptionPane.ERROR_MESSAGE);
		}
		
		@Override
		protected void close() {
			JOptionPane.showMessageDialog(null, "Nie można wykonać czynności: brak połączenia z serwerem.", "Błąd, (close)", JOptionPane.ERROR_MESSAGE);
		}
		
		@Override
		public void removeMessageListener(ConnectionListener listener) {
			JOptionPane.showMessageDialog(null, "Nie można wykonać czynności: brak połączenia z serwerem.", "Błąd, (removeMessageListener)", JOptionPane.ERROR_MESSAGE);
		}
	};

}
