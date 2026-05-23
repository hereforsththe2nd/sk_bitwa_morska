package client;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import client.ClientConnectionManager.AutoStopMessageListener;
import client.ClientConnectionManager.ConnectionListener;
import communication.ClientToServer;
import communication.Command;
import communication.CommandType;
import communication.GameClientToServer;
import communication.GameServerToClient;
import communication.ServerToClient;
import game.Board;
import game.Phase;

public class ClientGame extends JPanel{
	
	private static int tempNo=0;
	private boolean gameOngoing = false;
	Phase phase;
	
	Board yourBoard;
	Board oppBoard;
	
	ConnectionListener listener;
	
	private ClientConnectionManager conn;
	/**
	 * 
	 */
	private static final long serialVersionUID = 7597107787882315950L;

	public ClientGame(int width, int height) {		
		setVisible(false);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JButton temp = new JButton("Win " + tempNo);
		temp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					tempNo++;
					send(new Command(GameClientToServer.TEMP_PLEASELETMEWIN, ""));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		add(temp);
		JPanel left = new JPanel();
		JPanel right = new JPanel();
		add(left);
		add(right);
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		left.add(new JLabel("Ty"));
		right.add(new JLabel("Przeciwnik"));
		yourBoard = new Board(width, height);
		oppBoard = new Board(width, height);
		left.add(yourBoard);
		right.add(oppBoard);
		
		JPanel tiles = new JPanel();
		JPanel misc = new JPanel();
		left.add(tiles);
		right.add(misc);
	}
	
	protected void setConnection(ClientConnectionManager conn) {
		this.conn = conn;
		listener = new ConnectionListener() {
			@Override
			public void onMessage(Command com) {
				if(com.isContext(ServerToClient.GAME) && gameOngoing) {
					exec(Command.decode(com.body));
				}
			}
		};
		conn.addMessageListener(listener);
	}
	
	protected void startGame() {
		if(gameOngoing == true)
			throw new IllegalArgumentException("Gra już trwa.");
		if(conn == null)
			throw new IllegalArgumentException("Nie można zacząć gry, nie jsetem połączony ze serwerem"); 
		gameOngoing = true;
		setVisible(true);
	}
	
	private void exec(Command serverCom) {
		JOptionPane pane;
		JDialog dialog;
		Container parent =  getParent();
		Point location = getLocationOnScreen();
		switch(CommandType.get(serverCom.context, GameServerToClient.values())) {
		case GameServerToClient.YOU_LOST:
			pane = new JOptionPane("Przegrałeś " + serverCom.body, JOptionPane.INFORMATION_MESSAGE);
			dialog = pane.createDialog(this, "Koniec gry");
			dialog.setLocation(location.x+getWidth()/2-dialog.getWidth()/2, location.y+getHeight()/2-dialog.getHeight()/2);
			dialog.setModalityType(JDialog.ModalityType.MODELESS);
			dialog.setVisible(true);
			gameOngoing=false;
			setVisible(false);
			break;
		case GameServerToClient.YOU_WON:
			pane = new JOptionPane("Wygrałeś "  +serverCom.body, JOptionPane.INFORMATION_MESSAGE);
			dialog = pane.createDialog(this, "Koniec gry");
			dialog.setLocation(location.x+getWidth()/2-dialog.getWidth()/2, location.y+getHeight()/2-dialog.getHeight()/2);
			dialog.setModalityType(JDialog.ModalityType.MODELESS);
			dialog.setVisible(true);
			gameOngoing = false;
			setVisible(false);
			break;
		default:
			break;
		}

	}
	
	
	protected void send(Command comm) throws IOException {
		conn.send(new Command(ClientToServer.GAME, Command.encode(comm)));
	}

}
