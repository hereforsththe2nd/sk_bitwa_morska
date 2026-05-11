package clinet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import communication.ClientToServer;
import communication.Command;

public class Client extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2678921125605551080L;
	BufferedWriter bufferedWriter;
	Socket socket;
	public Client() throws UnknownHostException, IOException {
		setSize(500,500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		add(panel);
		
		JButton button = new JButton("Connect");
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(socket!=null)
						socket.close();
					socket = new Socket("localhost", 8000);
				    bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					System.out.println("Połączono się");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.add(button);
		
		JTextField send = new JTextField(20);
		send.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					System.out.println(send.getText());
					bufferedWriter.write(Command.encode(ClientToServer.CHAT, send.getText())+"\n");
					bufferedWriter.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.add(send);
		
	};
	
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