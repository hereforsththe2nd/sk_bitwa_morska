package client;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import client.ConnectionClient.ConnectionListener;
import communication.ClientToServer;
import communication.Command;
import communication.ServerToClient;

public class InviteButton extends JButton{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4100643046387856983L;

	public InviteButton(ConnectionClient conn, String userName, JLabel errors) {
		super("Zaproś");
		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					conn.send(new Command(ClientToServer.INVITE, userName));
				} catch (IOException e1) {
					errors.setText("Nie udało się wysłać zaproszenia.");
				}
			}
		});
	}
}
