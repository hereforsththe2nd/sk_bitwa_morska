package game;

import java.awt.Dimension;

import javax.swing.JPanel;

import communication.Command;

public class GameClient extends JPanel{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7597107787882315950L;

	public GameClient(int width, int height) {
		setPreferredSize(new Dimension(width, height));
	}
	
	public void exec(Command serverCom) {
		
	}
}
