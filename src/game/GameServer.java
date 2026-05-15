package game;

import java.awt.Dimension;

import javax.swing.JPanel;

public abstract class GameServer{
	abstract String encode();
	abstract void decode(String encoded);
	
}
