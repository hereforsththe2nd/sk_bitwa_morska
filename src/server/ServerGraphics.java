package server;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import communication.ClientToServer;
import communication.Command;

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		setSize(600,500);
		
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
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		namesPanel = new JPanel();
		namesScrollPane = new JScrollPane(namesPanel);
		namesScrollPane.setPreferredSize(new Dimension(100,100));
		namesPanel.add(new JTextField("ass"));
		add(namesScrollPane);
		
		JTextArea chatText = new JTextArea("CHAT\n");
		JScrollPane chatScroll = new JScrollPane(chatText);
		chatScroll.setPreferredSize(new Dimension(200,100));
		server.addMessageListener(new ClientToServerMessageListener() {
			
			@Override
			public void onMessage(Command command, User user) {
				if(command.type==ClientToServer.CHAT) {
					chatText.append(command.message+"\n");
					chatText.revalidate();
				}
			}
		});
		add(chatScroll);
		
		JPanel activity = new JPanel();
		activity.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JTextArea serverSendText = new JTextArea("send");
		serverSendText.setEditable(false);
		JTextArea serverRecieveText = new JTextArea("rec");
		serverRecieveText.setEditable(false);
		serverSendText.setPreferredSize(new Dimension(250,100));
		serverRecieveText.setPreferredSize(new Dimension(250,100));
		JScrollPane serverSendScrollPane = new JScrollPane(serverSendText);
		JScrollPane serverRecieveScrollPane = new JScrollPane(serverRecieveText);
		serverRecieveScrollPane.setSize(new Dimension(200,100));
		serverSendScrollPane.setSize(new Dimension(200,100));
		activity.add(serverRecieveScrollPane);
		activity.add(serverSendScrollPane);
		add(activity);
		server.addMessageListener(new ClientToServerMessageListener() {
			@Override
			public void onMessage(Command command, User user) {
				serverRecieveText.append("\n"+user.ID + "  " + user.userName + "  " + Command.encode(command));
				serverRecieveText.revalidate();
			}
		});
		
	}
	
	private void revalidateUsers() {
		namesPanel.removeAll();
		for(User user : server.users) {
			JTextArea userText = new JTextArea(user.userName);
			userText.setEditable(false);
			namesPanel.add(userText);
		}
		
		revalidate();
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				ServerGraphics frame;
				try {
					frame = new ServerGraphics();
					frame.setVisible(true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

}
