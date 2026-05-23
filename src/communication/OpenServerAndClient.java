package communication;

import java.awt.Point;
import java.io.IOException;

import javax.swing.SwingUtilities;

import client.Client;
import server.Server;

class OpenServerAndClient {
	public static void main(String[] args) {
	    final int clients = 2;
		for(int i=0;i<clients;i++) {
			int j=i;
		   SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						Client client = new Client();
						client.setLocation(new Point(j*client.getWidth(),0));
						client.setVisible(true);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			});
		}
	   	SwingUtilities.invokeLater(new Runnable() {
			
		   @Override
		   public void run() {
				Server frame;
				try {
					frame = new Server();
					frame.setLocation(700, 500);
					frame.setSize(700,400);
					frame.setVisible(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	   	});
	}
}
