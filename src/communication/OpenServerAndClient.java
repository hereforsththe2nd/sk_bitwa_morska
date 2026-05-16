package communication;

import java.io.IOException;

import javax.swing.SwingUtilities;

import client.Client;
import server.ServerGraphics;

class OpenServerAndClient {
	public static void main(String[] args) {
	    final int clients = 2;
		for(int i=0;i<clients ;i++) {
		   SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					try {
						Client client = new Client();
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
				ServerGraphics frame;
				try {
					frame = new ServerGraphics();
					frame.setVisible(true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	   	});
	}
}
