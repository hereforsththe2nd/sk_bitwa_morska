package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class Board extends JPanel {

	final int N = 10;
	private final Color color1=Color.WHITE;
	private final Color color2=Color.GRAY;
	private final Color hoverColor = Color.RED;
	int[] mousePosition = null;
	
	
	private JPanel main;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6889931911537334441L;
	
	public Board(int width, int height) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		main = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				Color recColor;
				int tileWidth = main.getWidth()/N;
				int tileHeight = main.getHeight()/N;
				int widthRest = main.getWidth()%N;
				int heightRest = main.getHeight()%N;
				for(int i=0;i<N;i++) {
					for(int j=0;j<N;j++) {
						if(mousePosition!=null && i==mousePosition[0] && j==mousePosition[1]) recColor=hoverColor;
						else if((i+j)%2 == 0)	recColor = color1;
						else	recColor=color2;
						g2d.setColor(recColor);
						if(i==N-1 && j==N-1) g2d.fillRect(tileWidth*i, tileHeight*j, tileWidth+widthRest, tileHeight+heightRest);
						else if(i==N-1) g2d.fillRect(tileWidth*i, tileHeight*j, tileWidth+widthRest, tileHeight);
						else if(j==N-1) g2d.fillRect(tileWidth*i, tileHeight*j, tileWidth, tileHeight+heightRest);
						else g2d.fillRect(tileWidth*i, tileHeight*j, tileWidth, tileHeight);
					}
				}
				
			}
		};
		main.setMaximumSize(new Dimension(width, height));
		//mainBoard.setPreferredSize(new Dimension(width, height));
		main.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		main.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				int xn = e.getX() * N/main.getWidth();
				int yn = e.getY() * N/main.getHeight();
				if(xn<0 || yn<0 || xn>=N || yn>=N) mousePosition = null;
				else mousePosition = new int[] {xn, yn};
				main.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				mouseMoved(e);
			}
		});
		main.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				mousePosition = null;
				main.repaint();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(main);
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
				frame.setVisible(true);
			}
		});
	}
	
	public static void main(String[] args) {
		test();
	}
}
