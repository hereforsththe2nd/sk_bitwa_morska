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

public class ClientGame extends JPanel {
    
    private static int tempNo=0;
    private boolean gameOngoing = false;
    Phase phase;
    
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
        
        DockFunctionality dockF = new DockFunctionality(yourBoard, dockBoard);
  
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
            
            dockBoard.setVisible(false);
            confirmPlacement.setVisible(false);
            
            JOptionPane.showMessageDialog(this, "Ustawienie zatwierdzone. Oczekiwanie na grę.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
            revalidate();
            repaint();
        });
        left.add(confirmPlacement);
        
        placeShips.addActionListener(e -> {
            yourBoard.getShips().clear();
            yourBoard.refreshGridShips();
            
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
    }
    
    protected void send(Command comm) throws IOException {
        conn.send(new Command(ClientToServer.GAME, Command.encode(comm)));
    }
}