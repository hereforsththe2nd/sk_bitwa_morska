package server;

import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ServerGraphics extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3519782776360303622L;

	JPanel panel;
	JScrollPane scrollPane;
	
	public ServerGraphics() throws IOException {
		super();
		setSize(100,100);
		panel = new JPanel();
		scrollPane = new JScrollPane(panel);
		panel.add(new JTextField("ass"));
		add(scrollPane);
		Server server = new Server(8000, new ConnectionListener() {
			
			@Override
			public void onConnection(Socket socket, User user) {
				JTextField text = new JTextField();
				text.setText(user.userName);
				panel.add(text);
				panel.revalidate();
			}
		}, new ClientToServerMessageListener() {
			
			@Override
			public void onMessage(ClientToServerCommand command, User user) {
				// TODO Auto-generated method stub
				
			}
		});
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
