package server;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

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
import communication.GameClientToServer;
import communication.ServerToClient;
import communication.User;

public class Server extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3519782776360303622L;

	JPanel namesPanel;
	JScrollPane namesScrollPane;
	ServerConnectionManager communication;
	
	public Server() throws IOException {
		super();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				communication.close();				
			}
		});
		setLayout(new FlowLayout());
		
		communication = new ServerConnectionManager(8000);
		communication.setConnectionListener( new ConnectionListener() {			
			@Override
			public void onConnection(Socket socket, User user) {
				try {
					communication.send(new Command(ServerToClient.CHAT, Command.encode(ChatList.SERVER, "Udało połączyć się ze serwerem.")), user);
				} catch (IOException e) {
					e.printStackTrace();
				}
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
		communication.addMessageListener(new ClientToServerMessageListener() {
			@Override
			public void onMessage(Command command, User user) {
				serverRecieveText.append("\n"+user.ID + "  " + user.userName + "  " + Command.encode(command));
				serverRecieveText.revalidate();
				try {
					switch(CommandType.get(command.context, ClientToServer.values())) {
					case ClientToServer.CHAT:
						chatText.append(command.body+"\n");
						chatText.revalidate();
							communication.sendAll(new Command(ServerToClient.CHAT, Command.encode(ChatList.CHAT, Command.encode(user.userName, command.body))));
						break;
					case ClientToServer.SET_USERNAME:
					caseBlock:
					{
						for(User us : communication.getUsers()) {
							if(us.userName.equals(command.body)) {
								communication.send(new Command(ServerToClient.ERROR_PANE, "Username "+command.body + " already in use."), user);
								break caseBlock;
							}
						}
						if(command.body.contains("|")) {
							communication.send(new Command(ServerToClient.ERROR_PANE, "Username cannot contain '|'."), user);
							break caseBlock;
							
						}
						if(command.body.equals("")) {
							communication.send(new Command(ServerToClient.ERROR_PANE, "Username cannot be empty."), user);
							break caseBlock;
						}
						String oldUserName = user.userName;
						user.setUserName(command.body);
						revalidateUsers();
						communication.sendAll(new Command(ServerToClient.CHAT, Command.encode(ChatList.SERVER, "User "+oldUserName+" changed their username to " + user.userName)));
						break caseBlock;
					}
					case ClientToServer.GET_USERS:
						String userNames = user.userName;
						for(User u : communication.getUsers())	
							if(u != user)	userNames+="|"+u.userName;
						communication.send(new Command(ServerToClient.USERLIST, userNames), user);
						break;
					case ClientToServer.INVITE:
					caseBlock:
					{
						User tryingToInvite = communication.getUser(command.body);
						if(tryingToInvite == null)
							communication.send(new Command(ServerToClient.ERROR_PANE, "User with the user name " + command.body + " does not exist."), user);
						else for(ServerGame game : ServerGame.games) {
								if(game.isPlaying(user)) {
									communication.send(new Command(ServerToClient.ERROR_PANE, "Już jesteś w grze!"), user);
									break caseBlock;
								}
								if(game.isPlaying(tryingToInvite)) {
									communication.send(new Command(ServerToClient.ERROR_PANE, "Użytkownik " + tryingToInvite.userName + " is already in a game."), user);
									break caseBlock;
								}
							}
						ServerGame.games.add(new ServerGame(user, tryingToInvite,communication ));
						communication.send(new Command(ServerToClient.START_GAME, "Zacząłeś grę z graczem " + tryingToInvite.userName), user);
						communication.send(new Command(ServerToClient.START_GAME, "Gracz " + user.userName + " zaczął z tobą grę."), tryingToInvite);
					}
						break;
					case ClientToServer.GAME:
						for(ServerGame game : ServerGame.games)
							if(game.isPlaying(user)) {
								game.exec(Command.decode(command.body), user);
								break;
							}
						break;
					default:
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		communication.addSendListener(new OutSendListener() {
			
			@Override
			public void onMessage(String target, Command command) {
				serverSendText.append("\n"+target + ": "+Command.encode(command));
			}
		});

	}
	
	static public final Object lock = new Object();
	private void revalidateUsers() {
		synchronized(lock) {
			namesPanel.removeAll();
			namesPanel.add(new JTextField("Users:"));
			for(User user : communication.getUsers()) {
				JTextArea userText = new JTextArea(user.userName);
				userText.setEditable(false);
				namesPanel.add(userText);
			}
			revalidate();
		}
	}
}
