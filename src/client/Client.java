package client;

import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import communication.ClientToServer;
import communication.Command;
import communication.CommandType;
import communication.ServerToClient;

public class Client extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2678921125605551080L;
	BufferedWriter bufferedWriter;
	Socket socket;
	private JPanel right;
	private JPanel leftToolbar;
	final JFrame frame = this;
	ConnectionClient conn;
	private JTextArea errors;
	Chat chat;
	
	public Client() throws UnknownHostException, IOException {		
		
		setSize(700,500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
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
		
		right = new JPanel();
		right.setBackground(Color.getHSBColor(0, 0, (float) 0.2));
		panel.add(right, BorderLayout.CENTER);
		
		//left
		errors = new JTextArea();
		errors.setEditable(false);
		errors.setForeground(Color.RED);
		errors.setPreferredSize(new Dimension(100, 50));
		leftToolbar.add(errors);
		
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
		
		chat = new Chat();
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
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(conn != null) {
						conn.close();
					}
					socket = new Socket(hostText.getText(), Integer.parseInt(portText.getText()));
					conn = new ConnectionClient(socket);
					conn.addConnectionListener(new ConnectionListener() {

						@Override
						public void onMessage(Command com) {
							switch(CommandType.get(com.context, ServerToClient.values())) {
							case ServerToClient.CHAT:
								Command message = Command.decode(com.body);
								chat.exec(message);
								break;
							case ServerToClient.ERROR_PANE:
								JOptionPane.showMessageDialog(frame, com.body, "", JOptionPane.ERROR_MESSAGE);
								break;
							default:
								errors.setText("Otrzymano od serwera: "+com.context);
								break;
							
							}
							if(com.context.equals(ServerToClient.CHAT.getLabel())) {
							}
						}
					});

					
				} catch (IOException e1) {
					errors.setText(e1.getLocalizedMessage());
					errors.revalidate();
				}
			}
		});
	}
	
	private void doTheMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenuItem setUsername = new JMenuItem("Ustaw nazwę użytkownika");
		JMenuItem clearChat = new JMenuItem("Wyczyść czat");
		menuBar.add(setUsername);
		menuBar.add(clearChat);
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
	}

	//private revalidateResize()
	
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
       */
   }
}