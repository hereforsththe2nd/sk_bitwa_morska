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
    
    Grid grid;
    java.util.List<Ship> ships = new java.util.ArrayList<>();
    
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
        
        grid.addMouseMotionListener(new MouseMotionAdapter() {
            
            @Override
            public void mouseMoved(MouseEvent e) {
            	super.mouseMoved(e);
                mousePosition = grid.getCoords(e.getX(), e.getY());
                hover.setPosition(mousePosition);
                grid.addRepaintRequest(HOVER);
                grid.repaint();
            }
            
        });
        
        grid.addMouseListener(new MouseAdapter() {            
            @Override
            public void mouseExited(MouseEvent e) {
            	super.mouseExited(e);
                hover.setPosition(null);
                grid.addRepaintRequest(HOVER);
                grid.repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
            	super.mouseReleased(e);
                selected.clear();
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
        add(grid, BoardLayout.Location.CENTER);
        add(leftColumn, BoardLayout.Location.LEFT);
        add(topRow, BoardLayout.Location.TOP);
    }
    

    public java.util.List<Ship> getShips() { return this.ships; }

    public void refreshGridShips() {
        grid.addRepaintRequest(SHIP);
        grid.repaint();
    }
    
    public boolean isValid(Ship ship) {
        for (int i = 0; i < ship.length; i++) {
            int x = ship.pos.x + (ship.horizontal ? i : 0);
            int y = ship.pos.y + (ship.horizontal ? 0 : i);

            if (x < 0 || y < 0 || x >= N || y >= N)
                return false;

            for (Ship other : ships) {
                if (other == ship || other.pos == null) continue;

                for (int j = 0; j < other.length; j++) {
                    int ox = other.pos.x + (other.horizontal ? j : 0);
                    int oy = other.pos.y + (other.horizontal ? 0 : j);

         
                    if (Math.abs(ox - x) <= 1 && Math.abs(oy - y) <= 1)
                        return false;
                }
            }
        }
        return true;
    }
    
    void addShip(Ship s) {
    	ships.add(s);
    	grid.addDrawable(s, SHIP);
    }
    
    void removeShip(Ship s) {
    	ships.remove(s);
    	grid.removeDrawable(s, SHIP);
    }
}