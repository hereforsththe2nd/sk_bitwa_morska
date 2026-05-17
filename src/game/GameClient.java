package game;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import client.ConnectionClient;
import client.ConnectionClient.ConnectionListener;
import communication.Command;
import communication.ServerToClient;

public class GameClient extends JPanel{
	
	Phase phase;
	
	Board yourBoard;
	Board oppBoard;
	
	private final ConnectionClient conn;
	/**
	 * 
	 */
	private static final long serialVersionUID = 7597107787882315950L;

	public GameClient(int width, int height, ConnectionClient conn) {
		this.conn = conn;
		conn.addMessageListener(new ConnectionListener() {
			@Override
			public void onMessage(Command com) {
				if(com.context.equals(ServerToClient.GAME.getLabel())) {
					exec(Command.decode(com.body));
				}
			}
		});
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
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
	
	private void exec(Command serverCom) {
		
	}
	
}
