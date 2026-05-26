package client;

import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import client.ClientConnectionManager.AutoStopMessageListener;
import client.ClientConnectionManager.ConnectionListener;
import client.ClientGame.Settings;
import communication.ClientToServer;
import communication.Command;
import communication.CommandType;
import communication.GameClientToServer;
import communication.ServerToClient;
import game.DockFunctionality;

public class Client extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2678921125605551080L;
	BufferedWriter bufferedWriter;
	Socket socket;
	private JPanel leftToolbar;
	final JFrame frame = this;
	private String userName = null;
	private JDialog showUsersPopUp;
	ClientConnectionManager conn = ClientConnectionManager.DISCONNECTED;
	private JLabel errors = new JLabel();
	Chat chat;
	private ClientGame game;
	
	//menu
	JCheckBoxMenuItem wrongShipSetPolicy;
	
	public Client() throws UnknownHostException, IOException {		
		
		setSize(700,500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Disconnected");
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		add(panel);
				
		JPanel left = new JPanel();
		left.setLayout(new BorderLayout());
		leftToolbar = new JPanel();
		leftToolbar.setBackground(Color.getHSBColor(0, 0, (float) 0.5));
		leftToolbar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		panel.add(left, BorderLayout.WEST);
		left.add(leftToolbar, BorderLayout.NORTH);
				
		//left
		//errors.setEditable(false);
		errors.setForeground(Color.RED);
		JScrollPane errorScrollPane = new JScrollPane(errors);
		errorScrollPane.setPreferredSize(new Dimension(100, 50));
		leftToolbar.add(errorScrollPane);
		
		JPanel connSpecs = new JPanel();
		JPanel connSpecsTop = new JPanel();
		JPanel connSpecsBottom = new JPanel();
		connSpecs.setLayout(new BorderLayout());
		connSpecs.add(connSpecsBottom, BorderLayout.SOUTH);
		connSpecs.add(connSpecsTop, BorderLayout.NORTH);
		connSpecsTop.add(new JLabel("host"), BorderLayout.EAST);
		connSpecsTop.add(new JLabel("port"), BorderLayout.WEST);
		JTextField hostText = new JTextField("localhost");
		JTextField portText = new JTextField("8000");
		connSpecsBottom.add(hostText, BorderLayout.EAST);
		connSpecsBottom.add(portText, BorderLayout.WEST);
		leftToolbar.add(connSpecs);
		
		JButton button = new JButton("Connect");
		leftToolbar.add(button);
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(conn != ClientConnectionManager.DISCONNECTED) {
					conn.close();
					conn = ClientConnectionManager.DISCONNECTED;
					button.setText("Connect");
					hostText.setEnabled(true);
					portText.setEnabled(true);
					frame.setTitle("Disconnected");
					game.exec(ClientGame.END_GAME);
					return;
				}
				try {
					socket = new Socket(hostText.getText(), Integer.parseInt(portText.getText()));
					conn = new ClientConnectionManager(socket);
					conn.addMessageListener(new ConnectionListener() {

						@Override
						public void onMessage(Command com) {
							switch(CommandType.get(com.context, ServerToClient.values())) {
							case ServerToClient.CHAT:
								Command message = Command.decode(com.body);
								chat.exec(message);
								break;
							case ServerToClient.START_GAME:
								JOptionPane pane = new JOptionPane(com.body, JOptionPane.INFORMATION_MESSAGE);
								JDialog dialog = pane.createDialog("Rozpoczęto grę");
								dialog.setModalityType(JDialog.ModalityType.MODELESS);
								dialog.setLocation(frame.getX()+frame.getWidth()/2-dialog.getWidth()/2, frame.getY()+frame.getHeight()/2-dialog.getHeight()/2);
								dialog.setVisible(true);
								Settings set = new Settings(wrongShipSetPolicy.getState() ? DockFunctionality.WrongMovePolicy.DONT_ALLOW : DockFunctionality.WrongMovePolicy.MAKE_NOTICABLE);
								game = new ClientGame(400, 400, set);
								game.setConnection(conn);
								panel.add(game, BorderLayout.CENTER);
								game.startGame();
								break;
							case ServerToClient.YOUR_USERNAME:
								userName = com.body;
								frame.setTitle(userName);
								break;
							case ServerToClient.ERROR_PANE:
								JOptionPane.showMessageDialog(frame, com.body, "", JOptionPane.ERROR_MESSAGE);
								break;
							default:
								errors.setText("Otrzymano od serwera: "+com.context);
								break;
							
							}
						}
					});
					frame.setTitle(userName);
					button.setText("Disconnect");
					hostText.setEnabled(false);
					portText.setEnabled(false);
				} catch (IOException e1) {
					errors.setText(e1.getLocalizedMessage());
					errors.revalidate();
				}
			}
		});

		chat = new Chat();
		DefaultCaret chatCaret = (DefaultCaret) chat.getCaret();
		chatCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		chat.setDisabledTextColor(Color.BLACK);
		chat.setEnabled(false);
		JScrollPane chatScroll = new  JScrollPane(chat);
		left.add(chatScroll, BorderLayout.CENTER);
		JTextField chatSend = new JTextField();
		chatSend.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					conn.send(new Command(ClientToServer.CHAT, chatSend.getText()));
					chatSend.setText("");
				} catch (IOException e1) {
					errors.setText(e1.getLocalizedMessage());
				}
			}
		});
		left.add(chatSend, BorderLayout.SOUTH);
		
		doTheMenu();
		
	}
	
	private void doTheMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenuItem setUsername = new JMenuItem("Ustaw nazwę użytkownika");
		JMenuItem clearChat = new JMenuItem("Wyczyść czat");
		JMenuItem showUsers = new JMenuItem("Pokaż użytkowników");
		JMenu settings = new JMenu("Ustawienia");
		menuBar.add(setUsername);
		menuBar.add(clearChat);
		menuBar.add(showUsers);
		menuBar.add(settings);
		setUsername.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = JOptionPane.showInputDialog("Wpisz nową nazwę użytkownika:");
				if(name != null) {
					try {
						conn.send(new Command(ClientToServer.SET_USERNAME, name));
					} catch (IOException e1) {						
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		clearChat.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				chat.clear();
			}
		});
		showUsers.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						if(showUsersPopUp != null) showUsersPopUp.dispose();
						showUsersPopUp = new JDialog(frame, "Połączeni użytkownicy", Dialog.ModalityType.MODELESS);
						JPanel showUsersPanel = new JPanel();

						try {
							conn.sendAndAwait(new Command(ClientToServer.GET_USERS, ""), new AutoStopMessageListener() {
								
								@Override
								public void onMessage(Command com) {
									showUsersPopUp.add(showUsersPanel);
									showUsersPanel.setLayout(new BoxLayout(showUsersPanel, BoxLayout.Y_AXIS));

									if(com.isContext(ServerToClient.USERLIST)) {
										String[] userNames = com.body.split("\\|");
										JPanel panel = new JPanel();
										panel.add(new JLabel(userNames[0]));
										panel.add(new JLabel("(Ty)"));
										showUsersPanel.add(panel);
										for(int i=1;i<userNames.length;i++) {
											String userName=userNames[i];
											panel = new JPanel();
											panel.add(new JLabel(userName));
											JButton butt = new InviteButton(conn, userName, errors);
											
											panel.add(butt);
											showUsersPanel.add(panel);
										}
									}
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
									}
									showUsersPopUp.pack();
									showUsersPopUp.setSize(
											showUsersPopUp.getWidth()+10, 
											showUsersPopUp.getHeight()+5
											);
									showUsersPopUp.setLocation(frame.getX()+frame.getWidth()/2-showUsersPopUp.getWidth()/2, frame.getY()+frame.getHeight()/2-showUsersPopUp.getHeight()/2);
									showUsersPopUp.setVisible(true);
								}
								
								@Override
								public boolean stop(Command message) {
									return message.isContext(ServerToClient.USERLIST);
								}
							});
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				});
			}
		});
		
		wrongShipSetPolicy = new JCheckBoxMenuItem("Automatycznie uniemożliwiać niemożliwe ustawienia statków?");
		settings.add(wrongShipSetPolicy);
		wrongShipSetPolicy.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(game!=null) {
					game.setWrongPlacementPolicy(wrongShipSetPolicy.getState() ? DockFunctionality.WrongMovePolicy.DONT_ALLOW : DockFunctionality.WrongMovePolicy.MAKE_NOTICABLE);
				}
			}
		});
	}

	//private revalidateResize()
	/*
	public static void main(String[] args) throws Exception{
	   SwingUtilities.invokeLater(new Runnable() {
		
		@Override
		public void run() {
			try {
				Client client = new Client();
				client.setVisible(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	});
       /*
       BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
       BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       bufferedWriter.write("Connected\n");
       bufferedWriter.flush();
       String line = bufferedReader.readLine();
       System.out.println(line);
       Thread.sleep(10000);
       socket.close();
       System.out.println("closed");
       
 	}
	}*/
}