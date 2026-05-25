package client;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
import game.Phase;
import game.PlayerBoard;
import game.Ship;

public class ClientGame extends JPanel {
    
	private ClientGame game = this;
	private DockFunctionality dockF;
    private static int tempNo=0;
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

    public ClientGame(int width, int height) {        
        setVisible(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        JButton temp = new JButton("Win " + tempNo);
        temp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    tempNo++;
                    send(new Command(GameClientToServer.TEMP_PLEASELETMEWIN, ""));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        add(temp);
        
        JButton placeShips = new JButton("Ustaw statki");
        add(placeShips);
        
        JPanel left = new JPanel();
        JPanel right = new JPanel();
        add(left);
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
        

        dockBoard.setVisible(false);
        left.add(dockBoard);
        

        confirmPlacement = new JButton("Zatwierdz ustawienie");
        confirmPlacement.setVisible(false);
        confirmPlacement.addActionListener(e -> {
         
            if (!dockBoard.getShips().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Rozmieść wszystkie statki", 
                    "Błąd ustawienia", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
            	String str = "";
            	for(Ship ship : yourBoard.getShips()) {
            		str += ship+"|";
            	}
            	sendAndAwait(new Command(GameClientToServer.SET_SHIP, str), new AutoStopMessageListener() {
					
					@Override
					public void onMessage(Command com) {
						if(com.isContext(GameServerToClient.WRONG_SHIP_CONFIGURATION)) {
				            JOptionPane.showMessageDialog(game, "Serwer nie zatwierdził ustawienia", "Niepowodzenie", JOptionPane.ERROR_MESSAGE);
				            return;
						}
						
						switch(CommandType.get(com.context, GameServerToClient.values())) {
						case GameServerToClient.PHASE_AWAIT:
							phase=Phase.AWAITING_MISSLE;
							break;
						case GameServerToClient.PHASE_SHOOT:
							phase=Phase.SENDING_MISSLE;
							break;
						default:
							return;
						}
						
			            dockBoard.setVisible(false);
			            confirmPlacement.setVisible(false);
			            placeShips.setVisible(false); 
			            if(dockF != null) {
			                dockF.dispose();
			                dockF = null;
			            }
			            
			            JOptionPane.showMessageDialog(game, "Ustawienie zatwierdzone. Oczekiwanie na grę.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
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
                return;
			}
           
        });
        left.add(confirmPlacement);
        
        placeShips.addActionListener(e -> {

            if(dockF != null) {
                dockF.dispose();
            }

            dockF = new DockFunctionality(
                    yourBoard,
                    dockBoard
            );
        	
            yourBoard.getShips().clear();
            yourBoard.refreshGridShips();
         
          
            dockBoard.refreshGridShips();
            dockBoard.startShipPlacement();
            dockBoard.setVisible(true);
            confirmPlacement.setVisible(true);
            
            revalidate();
            repaint();
        });
        
        JPanel tiles = new JPanel();
        JPanel misc = new JPanel();
        left.add(tiles);
        right.add(misc);
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
        default:
            break;
        }
    }
    
    private void endGame() {
        gameOngoing = false;
        setVisible(false);
        getParent().remove(this);
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

}