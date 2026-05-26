package game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import game.Drawables.Hover;
import game.Drawables.Tiles;
import game.Grid.MousePositionListener;

public class Board extends JPanel {

    public static final int TILE=0,
            SHIP=1,
            SIGN=2,
            HOVER=3;
    
    final int N;
    private final static String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    Position mousePosition = null;
    LinkedList<Position> selected = new LinkedList<Position>();
    
    Grid grid;
    protected LinkedList<Ship> ships = new LinkedList<Ship>();
    
    private static final long serialVersionUID = -6889931911537334441L;
    
    public Board(int width, int height, int N) {
    	this.N=N;
        grid = new Grid(N,N, 4);
        for(int x=0;x<getGrid().getN1();x+=1)
            for(int y=0;y<getGrid().getN2();y++)
                getGrid().addDrawable(new Tiles(new Position(x, y)), TILE);
        
        Hover hover = new Hover();
        getGrid().addDrawable(hover, HOVER);
        getGrid().setMaximumSize(new Dimension(width, height));
        getGrid().setPreferredSize(new Dimension(width, height));
        
        getGrid().addMouseMotionListener(new MouseMotionAdapter() {
            
            @Override
            public void mouseMoved(MouseEvent e) {
            	super.mouseMoved(e);
                mousePosition = getGrid().getCoords(e.getX(), e.getY());
                hover.setPosition(mousePosition);
                getGrid().addRepaintRequest(HOVER);
                getGrid().repaint();
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
            	super.mouseDragged(e);
            	mouseMoved(e);
            }
            
        });
        
        getGrid().addMouseListener(new MouseAdapter() {            
            @Override
            public void mouseExited(MouseEvent e) {
            	super.mouseExited(e);
                hover.setPosition(null);
                getGrid().addRepaintRequest(HOVER);
                getGrid().repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
            	super.mouseReleased(e);
                selected.clear();
                getGrid().repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
            	super.mouseClicked(e);
            	getGrid().flashDrawable(new Drawable() {
									
					@Override
					public Position getPosition() {
						return mousePosition;
					}
					
					@Override
					public void draw(Rectangle bounds, Graphics2D g2d) {
						g2d.setColor(new Color(50,50,50,200));
						g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
					}
				}, HOVER, 100);
            }
        });
        
        JPanel topRow = new JPanel() {
            private final static int YPAD = 2;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                FontMetrics fm = g2d.getFontMetrics();
                for(int i=0;i<N;i++) {
                    String column = String.valueOf(i+1);
                    int sw = fm.stringWidth(column);
                    g2d.drawString(column, (2*i+1)*getWidth()/N/2-sw/2,getHeight()-YPAD);
                }
            }
        };
        
        JPanel leftColumn = new JPanel() {
            final private static int XPAD = 2;
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
        add(getGrid(), BoardLayout.Location.CENTER);
        add(leftColumn, BoardLayout.Location.LEFT);
        add(topRow, BoardLayout.Location.TOP);
    }
    

    public void refreshGridShips() {
    	
        getGrid().addRepaintRequest(SHIP);
        getGrid().repaint();
    }
    
    
    void addShip(Ship s) {
    	ships.add(s);
    	for(Drawable d : s.tiles)
    		getGrid().addDrawable(d, SHIP);
    }
    
    public void removeShip(Ship s) {
    	ships.remove(s);
    	for(Drawable d : s.tiles)
    		getGrid().removeDrawable(d, SHIP);
    }
    
	public List<Ship> getShips() {
		return ships;
	}
	
	public void addMousePositionListener(MousePositionListener listener) {
		getGrid().addMousePosListener(listener);
	}


	public Grid getGrid() {
		return grid;
	}

}