package server;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import communication.ChatList;
import communication.ClientToServer;
import communication.Command;
import communication.CommandType;
import communication.ServerToClient;
import communication.User;

public class ServerGraphics extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3519782776360303622L;

	JPanel namesPanel;
	JScrollPane namesScrollPane;
	ConnectionManager server;
	
	public ServerGraphics() throws IOException {
		super();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				server.close();
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		setSize(800,600);
		setLayout(new FlowLayout());
		
		server = new ConnectionManager(8000, new ConnectionListener() {			
			@Override
			public void onConnection(Socket socket, User user) {
				System.out.println("Połączono się z użytkownikiem " + user.print());
				revalidateUsers();
			}

			@Override
			public void onDisconnect(Socket socket, User user) {
				revalidateUsers();
			}
		});
				
		namesPanel = new JPanel();
		namesPanel.setLayout(new BoxLayout(namesPanel, BoxLayout.Y_AXIS));
		namesPanel.add(new JTextField("Connected users:"));
		namesScrollPane = new JScrollPane(namesPanel);
		namesScrollPane.setPreferredSize(new Dimension(100,100));
		add(namesScrollPane);
		
		JTextArea chatText = new JTextArea("CHAT\n");
		JScrollPane chatScroll = new JScrollPane(chatText);
		chatScroll.setPreferredSize(new Dimension(200,100));
		add(chatScroll);
		
		JPanel activity = new JPanel();
		activity.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JTextArea serverSendText = new JTextArea("send");
		serverSendText.setEditable(false);
		JTextArea serverRecieveText = new JTextArea("rec");
		serverRecieveText.setEditable(false);
		JScrollPane serverSendScrollPane = new JScrollPane(serverSendText);
		JScrollPane serverRecieveScrollPane = new JScrollPane(serverRecieveText);
		serverRecieveScrollPane.setPreferredSize(new Dimension(300,150));
		serverSendScrollPane.setPreferredSize(new Dimension(300,150));
		activity.add(serverRecieveScrollPane);
		activity.add(serverSendScrollPane);
		add(activity);
		server.addMessageListener(new ClientToServerMessageListener() {
			@Override
			public void onMessage(Command command, User user) {
				serverRecieveText.append("\n"+user.ID + "  " + user.userName + "  " + Command.encode(command));
				serverRecieveText.revalidate();
				switch(CommandType.get(command.context, ClientToServer.values())) {
				case ClientToServer.CHAT:
					chatText.append(command.body+"\n");
					chatText.revalidate();
					try {
						server.sendAll(new Command(ServerToClient.CHAT, Command.encode(ChatList.CHAT, Command.encode(user.userName, command.body))));
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case ClientToServer.SET_USERNAME:
				caseBlock:
				{
					try {
						for(User us : server.getUsers()) {
							if(us.userName.equals(command.body)) {
								server.send(new Command(ServerToClient.ERROR_PANE, "Username "+command.body + " already in use."), user);
								break caseBlock;
							}
						}
						if(command.body.contains("|")) {
							server.send(new Command(ServerToClient.ERROR_PANE, "Username cannot contain '|'."), user);
							break caseBlock;
							
						}
						if(command.body.equals("")) {
							server.send(new Command(ServerToClient.ERROR_PANE, "Username cannot be empty."), user);
							break caseBlock;
						}
						String oldUserName = user.userName;
						user.setUserName(command.body);
						revalidateUsers();
						server.sendAll(new Command(ServerToClient.CHAT, Command.encode(ChatList.SERVER, "User "+oldUserName+" changed their username to " + user.userName)));
						break caseBlock;
					} catch(IOException e) {e.printStackTrace();}
				}
				default:
				}
			}
		});

	}
	
	static public final Object lock = new Object();
	private void revalidateUsers() {
		namesPanel.removeAll();
		namesPanel.add(new JTextField("Users:"));
		synchronized(lock){
			for(User user : server.getUsers()) {
				JTextArea userText = new JTextArea(user.userName);
				userText.setEditable(false);
				namesPanel.add(userText);
			}
		}
		revalidate();
	}
}
