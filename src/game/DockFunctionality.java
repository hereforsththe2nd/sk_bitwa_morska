package game;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

public class DockFunctionality {
	private final DockBoard dock;
	private final PlayerBoard board;
	
	private Ship dragged = null;
	private Board  startedDragging;
	private Position startLocation;
	
	private Board otherBoard(Board b) {
		if(b==dock)return board;
		if(b==board) return dock;
		throw new IllegalArgumentException("Board b must be contained within this DockFunctionality");
	}
	
	public DockFunctionality(PlayerBoard board, DockBoard dock) {
		this.board=board;
		this.dock=dock;
		
		//najpierw rzeczy które dla obydwóch są takie same
		for(Board b : new Board[] {board, dock}) {
			Board other = otherBoard(b);
			System.out.println("Added mouse listener for left presses");
			b.grid.addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					super.mouseDragged(e);
					if(dragged == null) return;
					if(startedDragging != b) return; //bo w oryginalnym kodzie kod ten borad na którym zaczęto ogarniał przesuwanie 
	                Point pMain = SwingUtilities.convertPoint(b.grid, e.getPoint(), other.grid);
	                Position posMain = other.grid.getCoords(pMain.x, pMain.y);
	                
	                if (posMain != null) {
	            
	                    if (!other.ships.contains(dragged)) {
	                        b.removeShip(dragged);
	                        b.refreshGridShips();
	                        other.addShip(dragged);
	                    }
	                    dragged.pos = posMain;
	                    other.refreshGridShips();
	                } else {
	      
	                    if (!b.ships.contains(dragged)) {
	                        other.removeShip(dragged);
	                        other.refreshGridShips();
	                        b.addShip(dragged);
	                    }
	                    Position pDock = b.grid.getCoords(e.getX(), e.getY());
	                    if (pDock != null) {
	                        dragged.pos = pDock;
	                    }
	                    b.refreshGridShips();
	                }
	
				}
			});
			b.grid.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					System.out.println("pressed");
					super.mousePressed(e);
					Position p = b.grid.getCoords(e.getX(), e.getY());
	                // LEWY KLIK = CHWYTANIE
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
			});
		}
		MouseAdapter onRelease = new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				if(dragged != null) {
					for(Ship s : board.ships) {
						if(board.isValid(s)) {
							s.setValid(true);
						}else {
							s.setValid(false);
						}
					}
					for(Ship s : dock.ships) {
						s.setValid(true);
					}
	            	dragged = null;
	                startedDragging=null;
	                startLocation=null;
	                board.refreshGridShips();
		            dock.layoutShipsInDock();
				}
			}

		};
		board.grid.addMouseListener(onRelease);
		dock.grid.addMouseListener(onRelease);
		
		//rzeczy które są różne dla obydwóch
		dock.grid.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				Position p = dock.grid.getCoords(e.getX(), e.getY());
                if (SwingUtilities.isRightMouseButton(e)) {
                    for (Ship s : dock.ships) {
                        if (s.occupies(p)) {
                            s.rotate();
                            boolean boundsOk = true;
                            for (int i = 0; i < s.length; i++) {
                                int x = s.pos.x + (s.horizontal ? i : 0);
                                int y = s.pos.y + (s.horizontal ? 0 : i);
                                if (x < 0 || y < 0 || x >= dock.N || y >= dock.N) boundsOk = false;
                            }
                            if (!boundsOk) s.rotate();
                            dock.layoutShipsInDock();
                        }
                    }
                }
			}
		});
		board.grid.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Position p = dock.grid.getCoords(e.getX(), e.getY());
				super.mousePressed(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    for (Ship s : board.ships) {
                        if (s.occupies(p)) {
                            s.rotate();
                            if (!board.isValid(s)) {
                                s.rotate(); 
                            } else {
                                board.refreshGridShips();
                            }
                            return;
                        }
                    }
                }

			}
			
		});
	}
}
