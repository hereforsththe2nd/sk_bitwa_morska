package server;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketExample {

   public static void main(String[] args) throws IOException {
       ServerSocket serverSocket = new ServerSocket(8000);
	   System.out.println("a");
       Socket socket = serverSocket.accept();
       BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
       System.out.println("c");
       
       bufferedWriter.write("Server says hi\n");
       bufferedWriter.flush();
       String line;
       int i=0;
       while(true) {
		   line = bufferedReader.readLine();
		   System.out.println(line+"1");
		   i++;
       }
       /*
       socket.close();
       serverSocket.close();*/
   }
}