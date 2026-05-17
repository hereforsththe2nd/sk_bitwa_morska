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


public class Board extends JPanel {

	final int N = 20;
	private final Color color1=new Color(192,149,55);
	private final Color color2=new Color(150,124,68);
	private final Color hoverColor = new Color(51, 49, 43, 150);
	private final Color hoverColor2 = new Color(51, 49, 70, 120);
	private final static String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	Position mousePosition = null;
	LinkedList<Position> selected = new LinkedList<Position>();
	
	private JPanel main;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6889931911537334441L;
	
	public Board(int width, int height) {
		main = new JPanel() {
			int tileWidth, tileHeight, widthRest, heightRest;
			
			private void fillRect(int i,int j, Color outline, Graphics2D g2d) {
				int bx1,by1,bx2,by2;
				if(i==N-1 && j==N-1) {
					bx1 = tileWidth*i;
					by1 = tileHeight*j;
					bx2 = tileWidth+widthRest;
					by2 = tileHeight+heightRest;
					
				}
				else if(i==N-1) {
					bx1 = tileWidth*i;
					by1 = tileHeight*j;
					bx2 = tileWidth+widthRest;
					by2 = tileHeight;
				}
				else if(j==N-1) { 
					bx1 = tileWidth*i;
					by1 = tileHeight*j;
					bx2 = tileWidth;
					by2 = tileHeight+heightRest;
				}
				else { 
					bx1 = tileWidth*i;
					by1 = tileHeight*j;
					bx2 = tileWidth;
					by2 = tileHeight;
				}
				g2d.fillRect(bx1, by1, bx2, by2);
				g2d.setColor(outline);
				g2d.drawRect(bx1, by1, bx2, by2);
			}
			
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				Color recColor;
				tileWidth = main.getWidth()/N;
				tileHeight = main.getHeight()/N;
				widthRest = main.getWidth()%N;
				heightRest = main.getHeight()%N;
				for(int i=0;i<N;i++) {
					for(int j=0;j<N;j++) {
						if((i+j)%2 == 0)	recColor = color1;
						else	recColor=color2;
						g2d.setColor(recColor);
						fillRect(i, j,Color.BLACK, g2d);
						if(mousePosition!=null && i==mousePosition.x && j==mousePosition.y) {
							g2d.setColor(hoverColor);
							fillRect(i, j, Color.BLACK, g2d);
						}
						if(selected.contains(new Position(i, j))) {
							g2d.setColor(hoverColor2);
							fillRect(i, j, Color.BLACK, g2d);
						}
					}
				}
				
			}
		};
		main.setMaximumSize(new Dimension(width, height));
		main.setPreferredSize(new Dimension(width, height));
		main.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
		main.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				int xn = e.getX() * N/main.getWidth();
				int yn = e.getY() * N/main.getHeight();
				if(xn<0 || yn<0 || xn>=N || yn>=N) mousePosition = null;
				else mousePosition = new Position(xn, yn);
				main.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				mouseMoved(e);
				if(mousePosition != null && !selected.contains(mousePosition))
					selected.add(mousePosition);
			}
			
			
		});
		main.addMouseListener(new MouseAdapter() {			
			@Override
			public void mouseExited(MouseEvent e) {
				mousePosition = null;
				main.repaint();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				if((selected.size() == 1 || selected.size() == 0) &&  mousePosition!=null)
					System.out.println("Clicked square " + mousePosition);
				selected.removeAll(selected);
				main.repaint();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				selected.add(mousePosition);
				main.repaint();
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
		add(main, BoardLayout.Location.CENTER);
		add(leftColumn, BoardLayout.Location.LEFT);
		add(topRow, BoardLayout.Location.TOP);
	}
	
	static private void test() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame();
				frame.setBackground(Color.magenta);
				frame.setSize(334,223);
				frame.getContentPane().setBackground(Color.green);
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
