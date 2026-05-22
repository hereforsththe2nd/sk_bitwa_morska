package game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import game.Drawables.Hover;
import game.Drawables.Tiles;


public class Board extends JPanel {

	public static final int TILE=0,
			SHIP=1,
			SIGN=2,
			HOVER=3;
	
	final int N = 10;
	private final static String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	Position mousePosition = null;
	LinkedList<Position> selected = new LinkedList<Position>();
	
	private Grid grid;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6889931911537334441L;
	
	public Board(int width, int height) {
		grid = new Grid(N,N, 4);
		for(int x=0;x<grid.getN1();x+=1)
			for(int y=0;y<grid.getN2();y++)
				grid.addDrawable(new Tiles(new Position(x, y)), TILE);
		
		Hover hover = new Hover();
		grid.addDrawable(hover, HOVER);
		grid.setMaximumSize(new Dimension(width, height));
		grid.setPreferredSize(new Dimension(width, height));
		grid.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				mousePosition = grid.getCoords(e.getX(), e.getY());
				hover.setPosition(mousePosition);
				grid.addRepaintRequest(HOVER);
				grid.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				mouseMoved(e);
				if(mousePosition != null && !selected.contains(mousePosition))
					selected.add(mousePosition);
			}
			
			
		});
		grid.addMouseListener(new MouseAdapter() {			
			@Override
			public void mouseExited(MouseEvent e) {
				hover.setPosition(null);
				grid.addRepaintRequest(HOVER);
				grid.repaint();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				if((selected.size() == 1 || selected.size() == 0) &&  mousePosition!=null)
					System.out.println("Clicked square " + mousePosition);
				selected.removeAll(selected);
				grid.repaint();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				selected.add(mousePosition);
				grid.repaint();
			}
		});
		
		JPanel topRow = new JPanel() {
			private final static int YPAD = 2;
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				FontMetrics fm = g2d.getFontMetrics();
				//int height = fm.getAscent()+fm.getDescent();
				for(int i=0;i<N;i++) {
					String column = String.valueOf(i+1);
					int sw = fm.stringWidth(column);
					g2d.drawString(column, (2*i+1)*getWidth()/N/2-sw/2,getHeight()-YPAD);
				}
			}
		};
		
		JPanel leftColumn = new JPanel() {
			final private static int  XPAD = 2;
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				FontMetrics fm = g2d.getFontMetrics();
				int height = fm.getAscent()+fm.getDescent();
				for(int i=0;i<N;i++) {
					String row = ALPHABET.charAt(i)+"";
					int sw = fm.stringWidth(row);
					g2d.drawString(row, getWidth()-sw-XPAD, getHeight()*(2*i+1)/2/N + height/2);
				}
			}
		};
		
		leftColumn.setBackground(Color.green);
		topRow.setBackground(Color.yellow);
		topRow.setMinimumSize(new Dimension(0,20));
		leftColumn.setMinimumSize(new Dimension(20,0));
		setLayout(new BoardLayout());
		add(grid, BoardLayout.Location.CENTER);
		add(leftColumn, BoardLayout.Location.LEFT);
		add(topRow, BoardLayout.Location.TOP);
	}
	
	static private void test() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame();
				frame.setSize(334,223);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				Board board = new Board(500, 500);
				frame.add(board);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
	
	public static void main(String[] args) {
		test();
	}
}
