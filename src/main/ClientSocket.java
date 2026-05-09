package main;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
   public static void main(String[] args) throws Exception{
       Socket socket = new Socket("localhost", 8002);
       BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
       BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       bufferedWriter.write("Connected\n");
       bufferedWriter.flush();
       String line = bufferedReader.readLine();
       System.out.println(line);
       Thread.sleep(10000);
       socket.close();
       System.out.println("closed");
   }
}