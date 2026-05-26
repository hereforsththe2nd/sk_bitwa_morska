package game;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class DockFunctionality {
	public enum WrongMovePolicy{
		MAKE_NOTICABLE, DONT_ALLOW,
	}
	private final DockFunctionality dockF = this;
    private final DockBoard dock;
    private final PlayerBoard board;
    WrongMovePolicy policy;
    private Ship dragged = null;
    private Board startedDragging;
    private Position startLocation;
    
    private final List<MouseListener> mListeners = new ArrayList<>();
    private final List<MouseMotionListener> mMotionListeners = new ArrayList<>();
    
    private Board otherBoard(Board b) {
        if(b == dock) return board;
        if(b == board) return dock;
        throw new IllegalArgumentException("Board b must be contained within this DockFunctionality");
    }
    
    public DockFunctionality(PlayerBoard board, DockBoard dock, WrongMovePolicy policy) {
    	this.policy = policy;
        this.board = board;
        this.dock = dock;
        
        for(Board b : new Board[] {board, dock}) {
            Board other = otherBoard(b);
            
          
            MouseAdapter dragAdapter = new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    super.mouseDragged(e);
                    if(dragged == null) return;
                    if(startedDragging != b) return; 
                    
                    Point pMain = SwingUtilities.convertPoint(b.getGrid(), e.getPoint(), other.getGrid());
                    Position posMain = other.getGrid().getCoords(pMain.x, pMain.y);
                    
                    if (posMain != null) {
                        if (!other.ships.contains(dragged)) {
                            b.removeShip(dragged);
                            b.refreshGridShips();
                            other.addShip(dragged);
                        }
                        dragged.pos.setValues(posMain);
                        other.refreshGridShips();
                    } else {
                        if (!b.ships.contains(dragged)) {
                            other.removeShip(dragged);
                            other.refreshGridShips();
                            b.addShip(dragged);
                        }
                        Position pDock = b.getGrid().getCoords(e.getX(), e.getY());
                        if (pDock != null) {
                            dragged.pos.setValues(pDock);
                        }
                        b.refreshGridShips();
                    }
                }
            };
            b.getGrid().addMouseMotionListener(dragAdapter);
            mMotionListeners.add(dragAdapter);
            
       
            MouseAdapter pressAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    Position p = b.getGrid().getCoords(e.getX(), e.getY());
                    if (p == null) return; 
                    
                    if(SwingUtilities.isLeftMouseButton(e)) {
                        for (Ship s : b.ships) {
                            if (s.occupies(p)) {
                                dragged = s;
                                startedDragging = b;
                                startLocation = p;
                                return;
                            }
                        }
                    }
                }
            };
            b.getGrid().addMouseListener(pressAdapter);
            mListeners.add(pressAdapter);
        }
        
    
        MouseAdapter onRelease = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(dragged != null) {
                    
                  
                	if(dockF.policy == WrongMovePolicy.DONT_ALLOW)
	                    if (board.ships.contains(dragged) && !board.isValid(dragged)) {
	                        board.removeShip(dragged);
	                        dock.addShip(dragged);
	                    }
                    
               
                    for(Ship s : board.ships) {
                        s.setValid(board.isValid(s));
                    }
                    for(Ship s : dock.ships) {
                        s.setValid(true);
                    }
                    
                    dragged = null;
                    startedDragging = null;
                    startLocation = null;
                    board.refreshGridShips();
                    dock.layoutShipsInDock();
                }
            }
        };
        board.getGrid().addMouseListener(onRelease);
        dock.getGrid().addMouseListener(onRelease);
        mListeners.add(onRelease);
        
   
        MouseAdapter dockRotateAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Position p = dock.getGrid().getCoords(e.getX(), e.getY());
                if (p == null) return;
                
                if (SwingUtilities.isRightMouseButton(e)) {
                    for (Ship s : dock.ships) {
                        if (s.occupies(p)) {
                            s.rotate();
                            boolean boundsOk = true;
                            for (int i = 0; i < s.length; i++) {
                                int x = s.pos.x + (s.horizontal.value ? i : 0);
                                int y = s.pos.y + (s.horizontal.value ? 0 : i);
                                if (x < 0 || y < 0 || x >= dock.N || y >= dock.N) boundsOk = false;
                            }
                            if (!boundsOk) s.rotate(); 
                            dock.refreshGridShips();
                            return; 
                        }
                    }
                }
            }
        };
        dock.getGrid().addMouseListener(dockRotateAdapter);
        mListeners.add(dockRotateAdapter);
        
       
        MouseAdapter boardRotateAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Position p = board.getGrid().getCoords(e.getX(), e.getY());
                if (p == null) return; 
                
                if (SwingUtilities.isRightMouseButton(e)) {
                	System.out.println(dockF.policy == WrongMovePolicy.DONT_ALLOW);
                    for (Ship s : board.ships) {
                        if (s.occupies(p)) {
                            s.rotate();
                            if (!board.isValid(s)) {
                            	if(dockF.policy == WrongMovePolicy.DONT_ALLOW)
                            		s.rotate(); 
                            	else if(dockF.policy == WrongMovePolicy.MAKE_NOTICABLE)
                            		s.setValid(false);
                            }
                            for(Ship sp : board.ships) {
                                sp.setValid(board.isValid(sp));
                            }
                            board.refreshGridShips();
                            return; 
                        }
                    }
                }
            }
        };
        board.getGrid().addMouseListener(boardRotateAdapter);
        mListeners.add(boardRotateAdapter);
    }
    
    public void dispose() {
        for (MouseListener ml : mListeners) {
            board.getGrid().removeMouseListener(ml);
            dock.getGrid().removeMouseListener(ml);
        }
        for (MouseMotionListener mml : mMotionListeners) {
            board.getGrid().removeMouseMotionListener(mml);
            dock.getGrid().removeMouseMotionListener(mml);
        }
        mListeners.clear();
        mMotionListeners.clear();
    }
    
    public void setWrongMovePolicy(WrongMovePolicy policy) {
    	this.policy=policy;
    }
}