package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Currency;

import javax.print.attribute.DocAttributeSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import client.ClientConnectionManager.AutoStopMessageListener;
import client.ClientConnectionManager.ConnectionListener;
import communication.ClientToServer;
import communication.Command;
import communication.CommandType;
import communication.GameClientToServer;
import communication.GameServerToClient;
import communication.ServerToClient;
import game.Board;
import game.DockBoard;
import game.DockFunctionality;
import game.DockFunctionality.WrongMovePolicy;
import game.Grid.MousePositionListener;
import game.Phase;
import game.PlayerBoard;
import game.Position;
import game.Ship;
import game.Ship.BooleanPointer;
import game.Drawables.Flash;
import game.Drawables.ShipTile;
import game.Drawables.X;
import game.Drawables.Text;
import game.Grid;

public class ClientGame extends JPanel {
	
	private ClientGame game = this;
    final JPanel left = new JPanel();
    final JPanel right = new JPanel();
    JButton placeShips;
    JLabel curentObjective;
    
	final Settings settings;
	private DockFunctionality dockF;
    private boolean gameOngoing = false;
    Phase phase = Phase.SETTING_SHIPS;
    
    PlayerBoard yourBoard;
    PlayerBoard/*może po prostu board to powinien być, ale na razie zostawię*/ oppBoard;
    DockBoard dockBoard; 
    JButton confirmPlacement;
    
    static final Command END_GAME = new Command("END_THE_GAME", "");
    ConnectionListener listener;
    private ClientConnectionManager conn;
    private static final long serialVersionUID = 7597107787882315950L;

    public ClientGame(int width, int height, Settings settings) {
    	this.settings = settings;
        setVisible(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));          
        add(left);
        add(Box.createHorizontalStrut(20));
        add(right);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        
        left.add(new JLabel("Ty"));
        right.add(new JLabel("Przeciwnik"));
        
        yourBoard = new PlayerBoard(width, height);
        oppBoard = new PlayerBoard(width, height);
        
     
        dockBoard = new DockBoard(width, (int)(height * 0.6));
        
  
        left.add(yourBoard);
        right.add(oppBoard);
        confirmPlacement = new JButton("Zatwierdz ustawienie");
        confirmPlacement.setVisible(false);
        left.add(confirmPlacement);

        dockBoard.setVisible(false);
        left.add(dockBoard);
        
        doSE();

        confirmPlacement.addActionListener(e -> {
         
            if (!dockBoard.getShips().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Rozmieść wszystkie statki", 
                    "Błąd ustawienia", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
            	String str = yourBoard.encodeShipLocations();
            	confirmPlacement.setEnabled(false);
            	placeShips.setEnabled(false);
	            if(dockF != null) {
	                dockF.dispose();
	                dockF = null;
	            }

            	sendAndAwait(new Command(GameClientToServer.SET_SHIP, str), new AutoStopMessageListener() {
					
					@Override
					public void onMessage(Command com) {
						if(com.isContext(GameServerToClient.WRONG_SHIP_CONFIGURATION)) {
				            JOptionPane.showMessageDialog(game, "Serwer nie zatwierdził ustawienia", "Niepowodzenie", JOptionPane.ERROR_MESSAGE);
				            confirmPlacement.setEnabled(true);
				            placeShips.setEnabled(true);
				            dockF = new DockFunctionality(
				                    yourBoard,
				                    dockBoard,
				                    settings.wrongPlacementPolicy
				            );

				            return;
						}
						
						switch(CommandType.get(com.context, GameServerToClient.values())) {
						case GameServerToClient.PHASE_AWAIT:
							setPhase(Phase.WAIT);;
				            JOptionPane.showMessageDialog(game, "Ustawienie zatwierdzone. Oczekiwanie aż przeciwnik ustawi statki.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
							break;
						case GameServerToClient.PHASE_SHOOT:
							setPhase(Phase.SENDING_MISSLE);
				            JOptionPane.showMessageDialog(game, "Ustawienie zatwierdzone. Przeciwnik ustawił statki. Możesz wykonać strzał.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
							break;
						default:
							return;
						}
						
			            dockBoard.setVisible(false);
			            confirmPlacement.setVisible(false);
			            placeShips.setVisible(false); 
			            
			            revalidate();
			            repaint();
					}
					
					@Override
					public boolean stop(Command message) {
						return message.isContext(GameServerToClient.PHASE_SHOOT) || message.isContext(GameServerToClient.PHASE_AWAIT) || message.isContext(GameServerToClient.WRONG_SHIP_CONFIGURATION);
					}
				});
			} catch (IOException e1) {
                JOptionPane.showMessageDialog(this, 
                        "Nie udało się wysłać rozmieszczenia statków", 
                        "Błąd ustawienia", JOptionPane.ERROR_MESSAGE);
	            confirmPlacement.setEnabled(true);
	            placeShips.setEnabled(true);
	            if(dockF != null)
		            dockF = new DockFunctionality(
		                    yourBoard,
		                    dockBoard,
		                    settings.wrongPlacementPolicy
		            );

                return;
			}
           
        });
        
        placeShips.addActionListener(e -> {

            if(dockF != null) {
                dockF.dispose();
            }
           
            yourBoard.clearShips();
            yourBoard.refreshGridShips();

            dockF = new DockFunctionality(
                    yourBoard,
                    dockBoard,
                    settings.wrongPlacementPolicy
            );
        	
         
          
            dockBoard.refreshGridShips();
            dockBoard.startShipPlacement();
            dockBoard.setVisible(true);
            confirmPlacement.setVisible(true);
            
            revalidate();
            repaint();
        });
        
        oppBoard.addMousePositionListener(new MousePositionListener() {
			
			@Override
			public void mouseClicked(Position p) {
				try {
					send(new Command(GameClientToServer.STRIKE, Position.encode(p)));
				} catch (IOException e) {
					JOptionPane.showMessageDialog(game, "Nie udało się wysłać inforamcji o strale serwerowi", "Błąd wysłania", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
    }
    
    protected void setConnection(ClientConnectionManager conn) {
        this.conn = conn;
        listener = new ConnectionListener() {
            @Override
            public void onMessage(Command com) {
                if(com.isContext(ServerToClient.GAME) && gameOngoing) {
                    exec(Command.decode(com.body));
                }
            }
        };
        conn.addMessageListener(listener);
    }
    
    protected void startGame() {
        if(gameOngoing == true)
            throw new IllegalArgumentException("Gra już trwa.");
        if(conn == null)
            throw new IllegalArgumentException("Nie można zacząć gry, nie jestem połączony z serwerem"); 
        gameOngoing = true;
        setVisible(true);
    }
    
    protected void exec(Command comm) {
        if(comm == END_GAME) {
            endGame();
            return;
        }
        JOptionPane pane;
        JDialog dialog;
        Point location = getLocationOnScreen();
		Position p;
        switch(CommandType.get(comm.context, GameServerToClient.values())) {
        case GameServerToClient.YOU_LOST:
            pane = new JOptionPane("Przegrałeś " + comm.body, JOptionPane.INFORMATION_MESSAGE);
            dialog = pane.createDialog(this, "Koniec gry");
            dialog.setLocation(location.x+getWidth()/2-dialog.getWidth()/2, location.y+getHeight()/2-dialog.getHeight()/2);
            dialog.setModalityType(JDialog.ModalityType.MODELESS);
            dialog.setVisible(true);
            endGame();
            break;
        case GameServerToClient.YOU_WON:
            pane = new JOptionPane("Wygrałeś "  +comm.body, JOptionPane.INFORMATION_MESSAGE);
            dialog = pane.createDialog(this, "Koniec gry");
            dialog.setLocation(location.x+getWidth()/2-dialog.getWidth()/2, location.y+getHeight()/2-dialog.getHeight()/2);
            dialog.setModalityType(JDialog.ModalityType.MODELESS);
            dialog.setVisible(true);
            endGame();
            break;
        case GameServerToClient.PHASE_AWAIT:
			setPhase(Phase.WAIT);
        	break;
        case GameServerToClient.PHASE_SHOOT:
			setPhase(Phase.SENDING_MISSLE);
        	break;
		case GameServerToClient.ENEMY_STRIKE:
			p = Position.decode(comm.body);
			yourBoard.getGrid().flashDrawable(new Flash(p, new Color(0,255,0,200)), Board.HOVER, 500);
			yourBoard.getGrid().addDrawable(new X(p), Board.SIGN);
			yourBoard.getGrid().addRepaintRequest(Board.SIGN);
			yourBoard.repaint();
			break;
		case GameServerToClient.STRIKE_HIT:
			p = Position.decode(comm.body);
			oppBoard.getGrid().addDrawable(new X(p), Board.SIGN);
			oppBoard.getGrid().addRepaintRequest(Board.SIGN);
			oppBoard.getGrid().addDrawable(new ShipTile(new Position(0,0), p, new BooleanPointer(true), null), Board.SHIP);
			oppBoard.getGrid().addRepaintRequest(Board.SHIP);
			oppBoard.repaint();
			break;
		case GameServerToClient.STRIKE_MISS:
			p = Position.decode(comm.body);
			System.out.println(p+"miss");
			oppBoard.getGrid().addDrawable(new X(p), Board.SIGN);
			oppBoard.getGrid().addRepaintRequest(Board.SIGN);
			oppBoard.repaint();
			break;
		case GameServerToClient.ERROR:
			JOptionPane.showMessageDialog(game, comm.body, "Serwer: błąd", JOptionPane.ERROR_MESSAGE);
			break;

        default:
            break;
        }
    }
    
    private void endGame() {
        gameOngoing = false;
        setVisible(false);
        getParent().remove(this);
    }
    
    private void setPhase(Phase p) {
    	this.phase = p;
    	if(p==Phase.WAIT)
    		curentObjective.setText("Czekanie na ruch przeciwnika");
    	if(p==Phase.SENDING_MISSLE)
    		curentObjective.setText("Strelaj!");
    }
    
    protected void send(Command comm) throws IOException {
        conn.send(new Command(ClientToServer.GAME, Command.encode(comm)));
    }
    
    protected void sendAndAwait(Command comm, AutoStopMessageListener listener) throws IOException {
        conn.sendAndAwait(new Command(ClientToServer.GAME, Command.encode(comm)), new AutoStopMessageListener() {
			
			@Override
			public void onMessage(Command com) {
				if(com.isContext(ServerToClient.GAME))
					listener.onMessage(Command.decode(com.body));
			}

			@Override
			public boolean stop(Command message) {
				if(message.isContext(ServerToClient.GAME))
						return listener.stop(Command.decode(message.body));
				return false;
			}
		});
    }

	void setWrongPlacementPolicy(WrongMovePolicy wrongPlacementPolicy) {
		this.settings.setWrongPlacementPolicy(wrongPlacementPolicy);
		if(dockF != null)
			dockF.setWrongMovePolicy(wrongPlacementPolicy);
	}
    
	public static void showTutorial() {
		try {
		JDialog tutorial = new JDialog(null, Dialog.ModalityType.MODELESS);
		JPanel left = new JPanel();
		JScrollPane leftScroll = new JScrollPane(left);
		JPanel right = new JPanel();
		
		Grid g = new Grid(5, 3, 1);
		//JPanel g = new JPanel();
		g.setBorder(BorderFactory.createLineBorder(Color.black, 5));
		int dx = 4;
		int tx = 2;
		g.addDrawable(new X(new Position(dx, 0)), 0);
		g.addDrawable(new ShipTile(new Position(dx, 1), new Position(0,0), new BooleanPointer(true), null), 0);
		g.addDrawable(new ShipTile(new Position(dx, 2), new Position(0,0), new BooleanPointer(true), null), 0);
		g.addDrawable(new X(new Position(dx, 2)), 0);
		
		g.addDrawable(new Text("Strzał:", new Position(tx,0)), 0);
		g.addDrawable(new Text("Statek:", new Position(tx,1)), 0);
		g.addDrawable(new Text("Zastrzelony:", new Position(tx,2)), 0);
		g.setPreferredSize(new Dimension(120,0));
		
		JTextArea text = new JTextArea();
		text.append("1)Połącz się ze serwerem\n");
		text.append("2)Kliknij \"Pokaż użytkowników\" aby zobaczyć możliwych przeciwników\n");
		text.append("3)Zaproś użytkownika\n");
		text.append("4)Kliknij ustaw statki\n");
		text.append("5)Po ustawieniu wszystkich statków kliknij zatwierdz ułożenie\n");
		text.append("W prawym dolnym rogu widzisz co aktualnie masz robić\n");
		text.append("6)Jeśli widzisz \"Strzelaj!\" kliknij na planszę przeciwnika\n");
		text.append("Ruch zmienia się za każdym razem, chyba że gracz trafi w statek: wtedy ma ponowny ruch.\n");
		
		text.setEditable(false);
		
		tutorial.setLayout(new BorderLayout());
		tutorial.add(leftScroll, BorderLayout.CENTER);
		leftScroll.setPreferredSize(new Dimension(text.getPreferredSize().width, text.getPreferredSize().height+30));
		tutorial.add(g, BorderLayout.EAST);
		left.add(text);
		//right.add(g);
		
		tutorial.setTitle("Pomoc");
		tutorial.pack();
		tutorial.setVisible(true);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static public class Settings{
		private WrongMovePolicy wrongPlacementPolicy;

		Settings(WrongMovePolicy wrongPlacementPolicy){
			this.wrongPlacementPolicy = wrongPlacementPolicy;
		}
		
		public WrongMovePolicy getWrongPlacementPolicy() {
			return wrongPlacementPolicy;
		}

		private void setWrongPlacementPolicy(WrongMovePolicy wrongPlacementPolicy) {
			this.wrongPlacementPolicy = wrongPlacementPolicy;
		}
	}
	
	private void doSE() {
        placeShips = new JButton("Ustaw statki");
        JButton forfeit = new JButton("Poddaj się");
        forfeit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    send(new Command(GameClientToServer.FORFEIT, ""));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        
        curentObjective = new JLabel("Ułóż statki");
        
        right.add(curentObjective);
        right.add(Box.createVerticalStrut(5));
        right.add(forfeit);
        right.add(Box.createVerticalStrut(5));
        right.add(placeShips);
       }
}